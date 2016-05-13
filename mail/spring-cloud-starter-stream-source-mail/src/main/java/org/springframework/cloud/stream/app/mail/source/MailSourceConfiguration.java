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
import org.springframework.integration.dsl.mail.MailInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.dsl.support.Transformers;
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

		IntegrationFlowBuilder flowBuilder;

		flowBuilder = getFlowBuilder();

		return flowBuilder.transform(Transformers.fromMail()).channel(source.output())
				.get();
	}

	/**
	 * Method to build Integration Flow for Mail. Suppress Warnings for
	 * MailInboundChannelAdapterSpec.
	 * @return Integration Flow object for Mail Source
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IntegrationFlowBuilder getFlowBuilder() {
		Properties javaMailProperties = this.properties.getJavaMailProperties();
		IntegrationFlowBuilder flowBuilder;
		if (this.properties.isIdleImap()) {
			flowBuilder = getIdleImapflow(this.properties.getMailUrl());
		}
		else {

			MailInboundChannelAdapterSpec adapterSpec;
			switch (this.properties.getProtocol().toUpperCase()) {
			case "IMAP":
			case "IMAPS":
				adapterSpec = getImapFlowBuilder(this.properties.getMailUrl());
				break;
			case "POP3":
				adapterSpec = getPop3FlowBuilder(this.properties.getMailUrl());
				break;
			default:
				throw new IllegalArgumentException(
						"Unsupported mail protocol: " + this.properties.getProtocol());
			}

			flowBuilder = IntegrationFlows.from(
					adapterSpec.javaMailProperties(javaMailProperties)
							.shouldDeleteMessages(this.properties.isDelete()),
					new Consumer<SourcePollingChannelAdapterSpec>() {

						@Override
						public void accept(
								SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
							sourcePollingChannelAdapterSpec.poller(defaultPoller);
						}
					});

		}
		return flowBuilder;
	}

	/**
	 * Method to build Integration flow for IMAP Idle configuration.
	 * @param mailURL Mail source URL.
	 * @return Integration Flow object IMAP IDLE.
	 */
	private IntegrationFlowBuilder getIdleImapflow(String mailURL) {
		IntegrationFlowBuilder flowBuilder = null;

		flowBuilder = IntegrationFlows.from(Mail.imapIdleAdapter(mailURL)
				.shouldDeleteMessages(this.properties.isDelete())
				.javaMailProperties(this.properties.getJavaMailProperties())
				.selectorExpression(this.properties.getExpression())
				.shouldMarkMessagesAsRead(this.properties.isMarkAsRead()));
		return flowBuilder;
	}

	/**
	 * Method to build Mail Channel Adapter for POP3.
	 * @param mailURL Mail source URL.
	 * @return Mail Channel for POP3
	 */
	@SuppressWarnings("rawtypes")
	private MailInboundChannelAdapterSpec getPop3FlowBuilder(String mailURL) {
		return Mail.pop3InboundAdapter(mailURL);
	}

	/**
	 * Method to build Mail Channel Adapter for IMAP.
	 * @param mailURL Mail source URL.
	 * @return Mail Channel for IMAP
	 */
	@SuppressWarnings("rawtypes")
	private MailInboundChannelAdapterSpec getImapFlowBuilder(String mailURL) {
		return Mail.imapInboundAdapter(mailURL)
				.shouldMarkMessagesAsRead(this.properties.isMarkAsRead());
	}

}
