/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.mail.source;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.mail.Mail;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.mail.transformer.MailToStringTransformer;
import org.springframework.integration.scheduling.PollerMetadata;

/**
 * A source module that listens for mail and emits the content as a message payload.
 *
 * @author Amol
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ MailSourceProperties.class })
@Import({ TriggerConfiguration.class })
public class MailSourceConfiguration {

	@Autowired
	@Qualifier("defaultPoller")
	PollerMetadata defaultPoller;

	@Autowired
	@Bindings(MailSourceConfiguration.class)
	Source source;

	@Autowired
	MailSourceProperties properties;

	@Bean
	public IntegrationFlow mailInboundFlow() {
		Properties javaMailProperties = new Properties();
		String mailURL = properties.getProtocol() + "://" + properties.getUsername() + ":"
				+ properties.getPassword() + "@" + properties.getHost() + ":"
				+ properties.getPort() + "/" + properties.getFolder();

		javaMailProperties.putAll(properties.parsePropertiesString());

		IntegrationFlowBuilder flowBuilder;
		if (properties.getProtocol().equalsIgnoreCase("imaps")
				|| properties.getProtocol().equalsIgnoreCase("imap")) {
			flowBuilder = IntegrationFlows.from(
					Mail.imapInboundAdapter(mailURL)
							.javaMailProperties(javaMailProperties)
							.shouldMarkMessagesAsRead(properties.isMarkAsRead())
							.shouldDeleteMessages(properties.isDelete()),
					new Consumer<SourcePollingChannelAdapterSpec>() {

						@Override
						public void accept(
								SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
							sourcePollingChannelAdapterSpec.poller(defaultPoller);
						}
					});

		}
		else {
			flowBuilder = IntegrationFlows.from(
					Mail.pop3InboundAdapter(mailURL)
							.javaMailProperties(javaMailProperties)
							.shouldDeleteMessages(properties.isDelete()),
					new Consumer<SourcePollingChannelAdapterSpec>() {

						@Override
						public void accept(
								SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
							sourcePollingChannelAdapterSpec.poller(defaultPoller);
						}
					});

		}

		return flowBuilder.transform(new MailToStringTransformer())
				.channel(source.output()).get();
	}

}
