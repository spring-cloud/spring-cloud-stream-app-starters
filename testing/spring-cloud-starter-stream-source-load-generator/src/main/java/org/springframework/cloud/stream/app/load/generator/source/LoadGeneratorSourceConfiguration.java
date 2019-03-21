/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.load.generator.source;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

/**
 * A source that sends a set amount of empty byte array messages to verify the speed
 * of the infrastructure.
 *
 * @author Glenn Renfro
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({LoadGeneratorSourceProperties.class})
public class LoadGeneratorSourceConfiguration extends AbstractEndpoint {

	private static final Log logger = LogFactory.getLog(LoadGeneratorSourceConfiguration.class);

	@Autowired
	private LoadGeneratorSourceProperties properties;

	@Autowired
	private Source channel;

	private final AtomicBoolean running = new AtomicBoolean(false);

	private volatile ExecutorService executorService;

	@Override
	protected void doStart() {
		if (running.compareAndSet(false, true)) {
			this.executorService = Executors.newFixedThreadPool(this.properties.getProducers());
			for (int i = 0; i < properties.getProducers(); i++) {
				this.executorService.execute(new Producer(i, this.channel,
						this.properties.getMessageCount(), this.properties.getMessageSize()));
			}
		}
	}
	@Override
	protected void doStop() {
		if (running.compareAndSet(true, false)) {
			executorService.shutdown();
		}
	}

	protected static class Producer implements Runnable {
		private final int producerId;

		private final Source channel;

		private final int messageCount;

		private final int messageSize;

		public Producer(int producerId, Source channel, int messageCount, int messageSize) {
			this.producerId = producerId;
			this.channel = channel;
			this.messageCount = messageCount;
			this.messageSize = messageSize;
		}

		@Override
		public void run() {
			logger.info(String.format("Producer %d sending %d messages", this.producerId, this.messageCount));
			Message<byte[]> message = new GenericMessage<>(new byte[this.messageSize]);
			for (int i = 0; i < this.messageCount; i++) {
				channel.output().send(message);
			}
			logger.info("All Messages Dispatched");
		}
	}

}
