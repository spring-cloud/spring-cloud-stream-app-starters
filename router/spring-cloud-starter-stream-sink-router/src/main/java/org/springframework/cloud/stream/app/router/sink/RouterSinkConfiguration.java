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

package org.springframework.cloud.stream.app.router.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.groovy.GroovyScriptExecutingMessageProcessor;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.integration.router.MethodInvokingRouter;
import org.springframework.integration.scripting.RefreshableResourceScriptSource;
import org.springframework.integration.scripting.ScriptVariableGenerator;
import org.springframework.scripting.ScriptSource;

/**
 * A sink app that routes to one or more named channels.
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(RouterSinkProperties.class)
public class RouterSinkConfiguration {

	@Autowired
	Sink channels;

	@Bean
	@ServiceActivator(inputChannel = Sink.INPUT)
	public AbstractMappingMessageRouter router(BinderAwareChannelResolver channelResolver,
			ScriptVariableGenerator scriptVariableGenerator, RouterSinkProperties properties) {
		AbstractMappingMessageRouter router;
		if (properties.getScript() != null) {
			router = new MethodInvokingRouter(scriptProcessor(scriptVariableGenerator, properties));
		}
		else {
			router = new ExpressionEvaluatingRouter(properties.getExpression());
		}
		router.setDefaultOutputChannelName(properties.getDefaultOutputChannel());
		router.setResolutionRequired(properties.isResolutionRequired());
		if (properties.getDestinationMappings() != null) {
			router.replaceChannelMappings(properties.getDestinationMappings());
		}
		router.setChannelResolver(channelResolver);
		return router;
	}

	@Bean
	@ConditionalOnProperty("router.script")
	public GroovyScriptExecutingMessageProcessor scriptProcessor(ScriptVariableGenerator scriptVariableGenerator,
			RouterSinkProperties properties) {
		ScriptSource scriptSource = new RefreshableResourceScriptSource(properties.getScript(),
				properties.getRefreshDelay());
		return new GroovyScriptExecutingMessageProcessor(scriptSource, scriptVariableGenerator);
	}

}
