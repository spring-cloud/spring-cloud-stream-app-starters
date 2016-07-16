package org.springframework.cloud.stream.app.yahooquotes.source;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import reactor.core.flow.Cancellation;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.Lifecycle;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Vinicius Carvalho
 */
public class YahooQuotesSourceMessageProducer implements Lifecycle {

	private YahooQuotesSourceProperties properties;

	private Lock lock = new ReentrantLock();
	private volatile boolean running;
	private Cancellation cancellation;
	private MessageChannel output;

	@Override
	public void start() {
		try{
			lock.lock();
			this.cancellation = Flux.interval(properties.getInterval())
					.flatMap(interval -> Flux.fromIterable(getSymbols()))
					.window(properties.getBatchSize())
					.flatMap(symbolFlux -> symbolFlux

							.map(StringUtils::quote)
							.collectList()
							.map(strings -> StringUtils.collectionToCommaDelimitedString(strings))
					).log()

					.subscribe(interval -> {
						output.send(MessageBuilder.withPayload(interval).build());
					});
		}finally {
			lock.unlock();
		}
	}

	@Override
	public void stop() {
		try{
			lock.lock();
			this.cancellation.dispose();
		}finally {
			lock.unlock();
		}
	}

	private Set<String> getSymbols(){
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
}
