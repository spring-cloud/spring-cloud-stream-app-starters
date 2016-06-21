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

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.junit.rabbit.RabbitTestSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for RabbitSink.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RabbitSinkTests.RabbitSinkApplication.class)
@DirtiesContext
public abstract class RabbitSinkTests {

	@ClassRule
	public static RabbitTestSupport rabbitAvailable = new RabbitTestSupport();

	@Autowired
	protected Sink channels;

	@Autowired
	protected RabbitSinkProperties properties;

	@Autowired
	protected RabbitTemplate rabbitTemplate;

	@Autowired
	protected RabbitAdmin rabbitAdmin;

	@Autowired(required = false)
	protected MessageConverter myConverter;

	@IntegrationTest({ "rabbit.routingKey=scsapp-testq",
		"rabbit.persistentDeliveryMode=true",
		"rabbit.mappedRequestHeaders=STANDARD_REQUEST_HEADERS,bar" })
	public static class SimpleRoutingKeyAndCustomHeaderTests extends RabbitSinkTests {

		@Test
		public void test() throws Exception {
			this.channels.input().send(MessageBuilder.withPayload("foo")
										.setHeader("bar", "baz")
										.setHeader("qux", "fiz")
										.build());
			this.rabbitTemplate.setReceiveTimeout(10000);
			Message received = this.rabbitTemplate.receive("scsapp-testq");
			assertEquals("foo", new String(received.getBody()));
			assertEquals("baz", received.getMessageProperties().getHeaders().get("bar"));
			assertNull(received.getMessageProperties().getHeaders().get("qux"));
			assertEquals(MessageDeliveryMode.PERSISTENT, received.getMessageProperties().getDeliveryMode());
			assertThat(this.rabbitTemplate.getMessageConverter(), instanceOf(SimpleMessageConverter.class));
		}

	}

	@IntegrationTest({ "rabbit.exchange=scsapp-testex",
		"rabbit.routingKey=scsapp-testrk",
		"rabbit.converterBeanName=myConverter",
		"rabbit.mappedRequestHeaders=STANDARD_REQUEST_HEADERS,bar" })
	public static class ExchangeRoutingKeyAndCustomHeaderTests extends RabbitSinkTests {

		@Test
		public void test() throws Exception {
			this.channels.input().send(MessageBuilder.withPayload("foo")
										.setHeader("bar", "baz")
										.setHeader(AmqpHeaders.DELIVERY_MODE, MessageDeliveryMode.PERSISTENT)
										.build());
			this.rabbitTemplate.setReceiveTimeout(10000);
			Message received = this.rabbitTemplate.receive("scsapp-testq");
			assertEquals("\"foo\"", new String(received.getBody()));
			assertEquals("baz", received.getMessageProperties().getHeaders().get("bar"));
			assertEquals(MessageDeliveryMode.PERSISTENT, received.getMessageProperties().getDeliveryMode());
			assertSame(this.myConverter, this.rabbitTemplate.getMessageConverter());
		}

	}

	@IntegrationTest({ "rabbit.exchangeExpression='scsapp-testex'",
		"rabbit.routingKeyExpression='scsapp-testrk'",
		"rabbit.converterBeanName=jsonConverter" })
	public static class ExchangeRoutingKeyExpressionsAndCustomHeaderTests extends RabbitSinkTests {

		@Test
		public void test() throws Exception {
			this.channels.input().send(MessageBuilder.withPayload("foo")
										.setHeader("bar", "baz")
										.setHeader("qux", "fiz")
										.build());
			this.rabbitTemplate.setReceiveTimeout(10000);
			Message received = this.rabbitTemplate.receive("scsapp-testq");
			assertEquals("\"foo\"", new String(received.getBody()));
			assertEquals("baz", received.getMessageProperties().getHeaders().get("bar"));
			assertEquals("fiz", received.getMessageProperties().getHeaders().get("qux"));
			assertEquals(MessageDeliveryMode.NON_PERSISTENT, received.getMessageProperties().getDeliveryMode());
		}

	}

	@SpringBootApplication
	static class RabbitSinkApplication {

		public static void main(String[] args) {
			SpringApplication.run(RabbitSinkApplication.class, args);
		}

		@Bean
		public Queue queue() {
			return new Queue("scsapp-testq", false, false, true);
		}

		@Bean
		public DirectExchange exchange() {
			return new DirectExchange("scsapp-testex", false, true);
		}

		@Bean
		public Binding binding() {
			return BindingBuilder.bind(queue()).to(exchange()).with("scsapp-testrk");
		}

		@Bean
		public Jackson2JsonMessageConverter myConverter() {
			return new Jackson2JsonMessageConverter();
		}

	}

}
