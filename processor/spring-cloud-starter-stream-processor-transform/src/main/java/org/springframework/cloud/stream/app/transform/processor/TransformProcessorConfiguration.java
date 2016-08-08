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

package org.springframework.cloud.stream.app.transform.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;

/**
 * A Processor app that transforms messages using a SpEL expression.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TransformProcessorProperties.class)
public class TransformProcessorConfiguration {

	@Autowired
	private TransformProcessorProperties properties;

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object transform(Message<?> message) {
		return properties.getExpression().getValue(message);
	}

}
