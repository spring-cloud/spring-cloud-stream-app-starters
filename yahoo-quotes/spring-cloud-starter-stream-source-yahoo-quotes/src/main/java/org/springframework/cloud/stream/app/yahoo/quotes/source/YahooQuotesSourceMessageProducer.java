/*
 *  Copyright 2016 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.cloud.stream.app.yahoo.quotes.source;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

import org.springframework.cloud.stream.app.yahoo.quotes.source.utils.PartitionedList;
import org.springframework.context.Lifecycle;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

/**
 * @author Vinicius Carvalho
 */
public class YahooQuotesSourceMessageProducer implements Lifecycle {

	private YahooQuotesSourceProperties properties;

	private Lock lock = new ReentrantLock();

	private volatile boolean running;

	private MessageChannel output;

	private ExecutorService pool;

	private BlockingQueue<Map<String, Object>> dispatchQueue;

	private YahooQuotesClient client;

	private PartitionedList<String> partitionedSymbols;

	private TokenBucket bucket;

	public YahooQuotesSourceMessageProducer(YahooQuotesClient client) {
		this.client = client;
	}

	@Override
	public void start() {
		try {
			lock.lock();
			loadSymbols();
			this.dispatchQueue = new ArrayBlockingQueue<>(10000);
			this.pool = Executors.newFixedThreadPool(3);
			if (!StringUtils.isEmpty(properties.getThrottle())) {
				this.bucket = TokenBuckets.builder()
						.withCapacity(properties.getThrottle())
						.withFixedIntervalRefillStrategy(properties.getThrottle(), 1,
								TimeUnit.SECONDS)
						.build();
			}
			pool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, Object> payload = null;
						while ((payload = dispatchQueue.take()) != null) {
							if (bucket != null)
								bucket.consume();
							output.send(MessageBuilder.withPayload(payload).build());
						}
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		finally {
			lock.unlock();
		}
	}

	@Scheduled(cron = "${yahoo.quotes.cronExpression:0 * 9-17 * * MON-FRI}", zone = "${yahoo.quotes.zone:EST}")
	public void poll() {
		for (List<String> symbols : partitionedSymbols) {
			pool.submit(new WebWorker(symbols));
		}
	}

	private void loadSymbols() {
		if (StringUtils.isEmpty(properties.getSymbols())) {
			loadDefaultSymbols();
		}
		else {
			this.partitionedSymbols = new PartitionedList<>(
					Arrays.asList(StringUtils
							.commaDelimitedListToStringArray(properties.getSymbols())),
					properties.getBatchSize());
		}

	}

	private void loadDefaultSymbols() {
		Scanner scanner = new Scanner(
				new InputStreamReader(YahooQuotesSourceMessageProducer.class
						.getClassLoader().getResourceAsStream("symbols")));
		LinkedList<String> symbols = new LinkedList<>();
		while (scanner.hasNext()) {
			symbols.add(scanner.nextLine());
		}
		scanner.close();
		this.partitionedSymbols = new PartitionedList<>(symbols,
				properties.getBatchSize());
	}

	@Override
	public void stop() {
		try {
			lock.lock();
			this.pool.shutdown();

		}
		finally {
			lock.unlock();
		}
	}

	private Set<String> getSymbols() {
		return StringUtils.commaDelimitedListToSet(properties.getSymbols());
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public void setOutput(MessageChannel output) {
		this.output = output;
	}

	public void setProperties(YahooQuotesSourceProperties properties) {
		this.properties = properties;
	}

	public class WebWorker implements Runnable {

		private List<String> symbols;

		public WebWorker(List<String> symbols) {
			this.symbols = symbols;
		}

		@Override
		public void run() {
			List<Map<String, Object>> results = client.fetchQuotes(symbols,
					properties.getFields());
			for (Map<String, Object> r : results) {
				try {
					dispatchQueue.put(r);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
