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

package org.springframework.cloud.stream.app.groovy.transform.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * A Processor app that transforms messages using a Groovy script.
 *
 * @author Eric Bottard
 * @author Mark Fisher
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(GroovyTransformProcessorProperties.class)
@Import(ScriptVariableGeneratorConfiguration.class)
public class GroovyTransformProcessorConfiguration {

	@Autowired
	private ScriptVariableGenerator scriptVariableGenerator;

	@Autowired
	private GroovyTransformProcessorProperties properties;

	@Bean
	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public MessageProcessor<?> transformer() {
		return new GroovyScriptExecutingMessageProcessor(
				new ResourceScriptSource(properties.getScript()), scriptVariableGenerator);
	}

}
