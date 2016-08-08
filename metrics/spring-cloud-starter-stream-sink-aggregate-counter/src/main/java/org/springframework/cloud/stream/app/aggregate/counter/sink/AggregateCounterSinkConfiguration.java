/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.aggregate.counter.sink;

import org.joda.time.DateTime;

import org.springframework.analytics.metrics.AggregateCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * Configuration class for Aggregate counter sink.
 *
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(AggregateCounterSinkProperties.class)
@Import(AggregateCounterSinkStoreConfiguration.class)
public class AggregateCounterSinkConfiguration {

	@Autowired
	private AggregateCounterSinkProperties config;

	@Autowired
	private AggregateCounterRepository aggregateCounterRepository;

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void process(Message<?> message) {
		Long increment = this.config.getIncrementExpression().getValue(message, Long.class);
		String counterName = this.config.getComputedNameExpression().getValue(message, String.class);
		if (this.config.getTimeField() == null) {
			this.aggregateCounterRepository.increment(counterName, increment, DateTime.now());
		}
		else {
			String timeStampValue = this.config.getTimeField().getValue(message, String.class);
			this.aggregateCounterRepository.increment(counterName, increment,
					this.config.getDateFormatter().parseDateTime(timeStampValue));
		}
	}
}
