/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.counter.sink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.EvaluationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * A simple module that counts messages received, using Spring Boot metrics abstraction.
 *
 * @author Eric Bottard
 * @author Mark Pollack
 * @author Marius Bogoevici
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(CounterProperties.class)
@Configuration
@Import(CounterSinkStoreConfiguration.class)
public class CounterSinkConfiguration {

	private static final Log logger = LogFactory.getLog(CounterSinkConfiguration.class);

	@Autowired
	private CounterService counterService;

	@Autowired
	private CounterProperties counterSinkProperties;

	@Autowired
	private EvaluationContext evaluationContext;

	@ServiceActivator(inputChannel=Sink.INPUT)
	public void count(Message<?> message) {
		String name = computeMetricName(message);
		logger.debug(String.format("Received: %s, about to increment counter named '%s'", message, name));
		counterService.increment(name);
	}

	protected String computeMetricName(Message<?> message) {
		if (counterSinkProperties.getName() != null) {
			return counterSinkProperties.getName();
		}
		else {
			return counterSinkProperties.getNameExpression().getValue(evaluationContext,
					message, CharSequence.class).toString();
		}
	}

}
