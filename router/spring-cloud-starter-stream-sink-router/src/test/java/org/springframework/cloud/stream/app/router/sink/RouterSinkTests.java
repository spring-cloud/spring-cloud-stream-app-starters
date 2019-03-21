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

package org.springframework.cloud.stream.app.router.sink;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.stream.test.binder.TestSupportBinder;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for RouterSinkConfiguration.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RouterSinkTests.RouterSinkApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true)
public abstract class RouterSinkTests {

	@Autowired
	protected Sink channels;

	@Autowired
	protected MessageCollector collector;

	@Autowired
	protected BinderFactory<MessageChannel> binderFactory;

	@Autowired
	protected AbstractMappingMessageRouter router;

	@IntegrationTest({ "router.resolutionRequired = true" })
	public static class DefaultRouterTests extends RouterSinkTests {

		@Test
		public void test() throws Exception {
			TestSupportBinder binder = (TestSupportBinder) this.binderFactory.getBinder(null);
			Message<?> message = MessageBuilder.withPayload("hello").setHeader("routeTo", "baz").build();
			this.channels.input().send(message);
			MessageChannel baz = binder.getChannelForName("baz");
			assertNotNull(baz);
			assertThat(collector.forChannel(baz), receivesPayloadThat(is("hello")));

			message = MessageBuilder.withPayload("world").setHeader("routeTo", "qux").build();
			this.channels.input().send(message);
			MessageChannel qux = binder.getChannelForName("qux");
			assertNotNull(qux);
			assertThat(collector.forChannel(qux), receivesPayloadThat(is("world")));
		}

	}

	@IntegrationTest({ "router.expression = headers['route']", "router.resolutionRequired = true" })
	public static class DefaultRouterWithExpressionTests extends RouterSinkTests {

		@Test
		public void test() throws Exception {
			TestSupportBinder binder = (TestSupportBinder) this.binderFactory.getBinder(null);
			Message<?> message = MessageBuilder.withPayload("hello").setHeader("route", "baz").build();
			this.channels.input().send(message);
			MessageChannel baz = binder.getChannelForName("baz");
			assertNotNull(baz);
			assertThat(collector.forChannel(baz), receivesPayloadThat(is("hello")));

			message = MessageBuilder.withPayload("world").setHeader("route", "qux").build();
			this.channels.input().send(message);
			MessageChannel qux = binder.getChannelForName("qux");
			assertNotNull(qux);
			assertThat(collector.forChannel(qux), receivesPayloadThat(is("world")));
		}

	}

	@IntegrationTest({ "router.expression = headers['route']", "router.destinationMappings = foo=baz \\n bar=qux",
			"router.resolutionRequired = true" })
	public static class WithChannelMappingsTests extends RouterSinkTests {

		@Test
		public void test() throws Exception {
			TestSupportBinder binder = (TestSupportBinder) this.binderFactory.getBinder(null);
			Message<?> message = MessageBuilder.withPayload("hello").setHeader("route", "foo").build();
			this.channels.input().send(message);
			MessageChannel baz = binder.getChannelForName("baz");
			assertNotNull(baz);
			assertThat(collector.forChannel(baz), receivesPayloadThat(is("hello")));

			message = MessageBuilder.withPayload("world").setHeader("route", "bar").build();
			this.channels.input().send(message);
			MessageChannel qux = binder.getChannelForName("qux");
			assertNotNull(qux);
			assertThat(collector.forChannel(qux), receivesPayloadThat(is("world")));
		}

	}

	@IntegrationTest({ "router.expression = headers['route']", "router.defaultOutputChannel = discards",
		"spring.cloud.stream.dynamicDestinations = foo,bar,discards" })
	public static class WithDiscardChannelTests extends RouterSinkTests {

		@Test
		public void test() throws Exception {
			TestSupportBinder binder = (TestSupportBinder) this.binderFactory.getBinder(null);
			Message<?> message = MessageBuilder.withPayload("hello").setHeader("route", "foo").build();
			this.channels.input().send(message);
			message = MessageBuilder.withPayload("hello").setHeader("route", "bar").build();
			this.channels.input().send(message);
			message = MessageBuilder.withPayload("hello").setHeader("route", "baz").build();
			this.channels.input().send(message);
			MessageChannel foo = binder.getChannelForName("foo");
			assertNotNull(foo);
			MessageChannel bar = binder.getChannelForName("bar");
			assertNotNull(bar);
			MessageChannel baz = binder.getChannelForName("baz");
			assertNull(baz);
			MessageChannel discards = binder.getChannelForName("discards");
			assertNotNull(discards);
			assertThat(collector.forChannel(foo), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(bar), receivesPayloadThat(is("hello")));
			assertThat(collector.forChannel(discards), receivesPayloadThat(is("hello")));
		}

	}

	@IntegrationTest({ "router.script = classpath:/routertest.groovy", "router.variables = foo=baz",
		"router.variablesLocation = classpath:/routertest.properties" })
	public static class WithGroovyTests extends RouterSinkTests {

		@Test
		public void test() throws Exception {
			TestSupportBinder binder = (TestSupportBinder) this.binderFactory.getBinder(null);
			Message<?> message = MessageBuilder.withPayload("hello").setHeader("route", "foo").build();
			this.channels.input().send(message);
			MessageChannel baz = binder.getChannelForName("baz");
			assertNotNull(baz);
			assertThat(collector.forChannel(baz), receivesPayloadThat(is("hello")));

			message = MessageBuilder.withPayload("world").setHeader("route", "bar").build();
			this.channels.input().send(message);
			MessageChannel qux = binder.getChannelForName("qux");
			assertNotNull(qux);
			assertThat(collector.forChannel(qux), receivesPayloadThat(is("world")));
		}

	}

	@SpringBootApplication
	public static class RouterSinkApplication {

	}

}
