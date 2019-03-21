/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.app.rabbit.source;

import org.aopalliance.aop.Advice;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.Listener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.Assert;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 * A source module that receives data from RabbitMQ.
 *
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(RabbitSourceProperties.class)
public class RabbitSourceConfiguration {

	private static final MessagePropertiesConverter inboundMessagePropertiesConverter =
			new DefaultMessagePropertiesConverter() {

				@Override
				public MessageProperties toMessageProperties(AMQP.BasicProperties source, Envelope envelope,
						String charset) {
					MessageProperties properties = super.toMessageProperties(source, envelope, charset);
					properties.setDeliveryMode(null);
					return properties;
				}

			};

	@Autowired
	@Bindings(RabbitSourceConfiguration.class)
	private Source channels;

	@Autowired
	private RabbitProperties rabbitProperties;

	@Autowired
	private RabbitSourceProperties properties;

	@Autowired
	private ConnectionFactory rabbitConnectionFactory;

	@Bean
	public SimpleMessageListenerContainer container() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(this.rabbitConnectionFactory);
		Listener listenerProperties = this.rabbitProperties.getListener();
		AcknowledgeMode acknowledgeMode = listenerProperties.getAcknowledgeMode();
		if (acknowledgeMode != null) {
			container.setAcknowledgeMode(acknowledgeMode);
		}
		Integer concurrency = listenerProperties.getConcurrency();
		if (concurrency != null) {
			container.setConcurrentConsumers(concurrency);
		}
		Integer maxConcurrency = listenerProperties.getMaxConcurrency();
		if (maxConcurrency != null) {
			container.setMaxConcurrentConsumers(maxConcurrency);
		}
		Integer prefetch = listenerProperties.getPrefetch();
		if (prefetch != null) {
			container.setPrefetchCount(prefetch);
		}
		Integer transactionSize = listenerProperties.getTransactionSize();
		if (transactionSize != null) {
			container.setTxSize(transactionSize);
		}
		container.setDefaultRequeueRejected(this.properties.getRequeue());
		container.setChannelTransacted(this.properties.getTransacted());
		String[] queues = this.properties.getQueues();
		Assert.state(queues.length > 0, "At least one queue is required");
		Assert.noNullElements(queues, "queues cannot have null elements");
		container.setQueueNames(queues);
		if (this.properties.isEnableRetry()) {
			container.setAdviceChain(new Advice[] { rabbitSourceRetryInterceptor() });
		}
		container.setMessagePropertiesConverter(inboundMessagePropertiesConverter);
		return container;
	}

	@Bean
	public AmqpInboundChannelAdapter adapter() {
		AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container());
		adapter.setOutputChannel(channels.output());
		DefaultAmqpHeaderMapper headerMapper = new DefaultAmqpHeaderMapper();
		headerMapper.setRequestHeaderNames(this.properties.getMappedRequestHeaders());
		adapter.setHeaderMapper(headerMapper);
		return adapter;
	}

	@Bean
	public RetryOperationsInterceptor rabbitSourceRetryInterceptor() {
		return RetryInterceptorBuilder.stateless()
				.maxAttempts(this.properties.getMaxAttempts())
				.backOffOptions(this.properties.getInitialRetryInterval(), this.properties.getRetryMultiplier(),
						this.properties.getMaxRetryInterval())
				.recoverer(new RejectAndDontRequeueRecoverer())
				.build();
	}

}
