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

import javax.mail.URLName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultOne;
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
 * @author Artem Bilan
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ MailSourceProperties.class })
@Import({ TriggerConfiguration.class })
public class MailSourceConfiguration {

	@Autowired
	@Qualifier("defaultPoller")
	private PollerMetadata defaultPoller;

	@Autowired
	private MailSourceProperties properties;

	@Bean
	public TriggerProperties triggerProperties() {
		return new TriggerPropertiesMaxMessagesDefaultOne();
	}

	@Bean
	public IntegrationFlow mailInboundFlow() {
		return getFlowBuilder()
				.transform(Transformers.fromMail(this.properties.getCharset()))
				.channel(Source.OUTPUT)
				.get();
	}

	/**
	 * Method to build Integration Flow for Mail. Suppress Warnings for
	 * MailInboundChannelAdapterSpec.
	 * @return Integration Flow object for Mail Source
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IntegrationFlowBuilder getFlowBuilder() {

		IntegrationFlowBuilder flowBuilder;
		URLName urlName = this.properties.getMailUrl();

		if (this.properties.isIdleImap()) {
			flowBuilder = getIdleImapFlow(urlName);
		}
		else {

			MailInboundChannelAdapterSpec adapterSpec;
			switch (urlName.getProtocol().toUpperCase()) {
				case "IMAP":
				case "IMAPS":
					adapterSpec = getImapFlowBuilder(urlName);
					break;
				case "POP3":
				case "POP3S":
					adapterSpec = getPop3FlowBuilder(urlName);
					break;
				default:
					throw new IllegalArgumentException(
							"Unsupported mail protocol: " + urlName.getProtocol());
			}
			flowBuilder = IntegrationFlows.from(
					adapterSpec.javaMailProperties(getJavaMailProperties(urlName))
							.selectorExpression(this.properties.getExpression())
							.shouldDeleteMessages(this.properties.isDelete()),
					new Consumer<SourcePollingChannelAdapterSpec>() {

						@Override
						public void accept(
								SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
							sourcePollingChannelAdapterSpec.poller(MailSourceConfiguration.this.defaultPoller);
						}

					});

		}
		return flowBuilder;
	}

	/**
	 * Method to build Integration flow for IMAP Idle configuration.
	 * @param urlName Mail source URL.
	 * @return Integration Flow object IMAP IDLE.
	 */
	private IntegrationFlowBuilder getIdleImapFlow(URLName urlName) {
		return IntegrationFlows.from(Mail.imapIdleAdapter(urlName.toString())
				.shouldDeleteMessages(this.properties.isDelete())
				.javaMailProperties(getJavaMailProperties(urlName))
				.selectorExpression(this.properties.getExpression())
				.shouldMarkMessagesAsRead(this.properties.isMarkAsRead()));
	}

	/**
	 * Method to build Mail Channel Adapter for POP3.
	 * @param urlName Mail source URL.
	 * @return Mail Channel for POP3
	 */
	@SuppressWarnings("rawtypes")
	private MailInboundChannelAdapterSpec getPop3FlowBuilder(URLName urlName) {
		return Mail.pop3InboundAdapter(urlName.toString());
	}

	/**
	 * Method to build Mail Channel Adapter for IMAP.
	 * @param urlName Mail source URL.
	 * @return Mail Channel for IMAP
	 */
	@SuppressWarnings("rawtypes")
	private MailInboundChannelAdapterSpec getImapFlowBuilder(URLName urlName) {
		return Mail.imapInboundAdapter(urlName.toString())
				.shouldMarkMessagesAsRead(this.properties.isMarkAsRead());
	}

	private Properties getJavaMailProperties(URLName urlName) {
		Properties javaMailProperties = new Properties();

		switch (urlName.getProtocol().toUpperCase()) {
			case "IMAP":
				javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.SocketFactory");
				javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
				javaMailProperties.setProperty("mail.store.protocol", "imap");
				break;

			case "IMAPS":
				javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
				javaMailProperties.setProperty("mail.store.protocol", "imaps");
				break;

			case "POP3":
				javaMailProperties.setProperty("mail.pop3.socketFactory.class", "javax.net.SocketFactory");
				javaMailProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
				javaMailProperties.setProperty("mail.store.protocol", "pop3");
				break;

			case "POP3S":
				javaMailProperties.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				javaMailProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
				javaMailProperties.setProperty("mail.store.protocol", "pop3s");
				break;
		}

		javaMailProperties.putAll(this.properties.getJavaMailProperties());
		return javaMailProperties;
	}

}
