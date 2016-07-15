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

package org.springframework.cloud.stream.app.rabbit.sink;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

/**
 * A sink module that sends data to RabbitMQ.
 *
 * @author Gary Russell
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(RabbitSinkProperties.class)
public class RabbitSinkConfiguration {

	@Autowired
	private RabbitSinkProperties properties;

	@Value("#{${rabbit.converterBeanName:null}}")
	private MessageConverter messageConverter;

	@ServiceActivator(inputChannel = Sink.INPUT)
	@Bean
	public MessageHandler amqpChannelAdapter(ConnectionFactory rabbitConnectionFactory) {
		AmqpOutboundEndpoint handler = new AmqpOutboundEndpoint(rabbitTemplate(rabbitConnectionFactory));
		DefaultAmqpHeaderMapper mapper = new DefaultAmqpHeaderMapper();
		mapper.setRequestHeaderNames(this.properties.getMappedRequestHeaders());
		handler.setHeaderMapper(mapper);
		handler.setDefaultDeliveryMode(this.properties.getPersistentDeliveryMode()
										? MessageDeliveryMode.PERSISTENT
										: MessageDeliveryMode.NON_PERSISTENT);
		if (this.properties.getExchangeExpression() == null) {
			handler.setExchangeName(this.properties.getExchange());
		}
		else {
			handler.setExpressionExchangeName(this.properties.getExchangeExpression());
		}
		if (this.properties.getRoutingKeyExpression() == null) {
			handler.setRoutingKey(this.properties.getRoutingKey());
		}
		else {
			handler.setExpressionRoutingKey(this.properties.getRoutingKeyExpression());
		}
		return handler;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory);
		if (this.messageConverter != null) {
			rabbitTemplate.setMessageConverter(this.messageConverter);
		}
		return rabbitTemplate;
	}

	@Bean
	@ConditionalOnProperty(name = "rabbit.converterBeanName", havingValue = RabbitSinkProperties.JSON_CONVERTER)
	public Jackson2JsonMessageConverter jsonConverter() {
		return new Jackson2JsonMessageConverter();
	}

}
