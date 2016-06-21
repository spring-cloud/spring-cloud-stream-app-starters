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

package org.springframework.cloud.stream.app.jms.source;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javax.jms.Session;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.jms.JmsMessageDrivenEndpoint;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for JmsSource.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JmsSourceTests.JmsSourceApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true)
public abstract class JmsSourceTests {

	@Autowired
	protected Source channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected JmsSourceProperties properties;

	@Autowired
	protected JmsMessageDrivenEndpoint endpoint;

	@IntegrationTest({ "jms.sessionTransacted = false", "jms.clientId = client", "jms.destination = topic",
						"jms.messageSelector = JMSCorrelationId=foo",
						"jms.subscriptionDurable = false", "jms.subscriptionShared = false",
						"spring.jms.listener.acknowledgeMode = DUPS_OK",
						"spring.jms.listener.concurrency = 3",
						"spring.jms.listener.maxConcurrency = 4",
						"spring.jms.pubSubDomain = true" })
	public static class PropertiesPopulated1Tests extends JmsSourceTests {

		@Test
		public void test() throws Exception {
			AbstractMessageListenerContainer container = TestUtils.getPropertyValue(this.endpoint, "listenerContainer",
					AbstractMessageListenerContainer.class);
			assertThat(container, instanceOf(SimpleMessageListenerContainer.class));
			assertEquals(Session.DUPS_OK_ACKNOWLEDGE, TestUtils.getPropertyValue(container, "sessionAcknowledgeMode"));
			assertFalse(TestUtils.getPropertyValue(container, "sessionTransacted", Boolean.class));
			assertEquals("client", TestUtils.getPropertyValue(container, "clientId"));
			assertEquals("topic", TestUtils.getPropertyValue(container, "destination"));
			assertEquals("JMSCorrelationId=foo", TestUtils.getPropertyValue(container, "messageSelector"));
			assertFalse(TestUtils.getPropertyValue(container, "subscriptionDurable", Boolean.class));
			assertFalse(TestUtils.getPropertyValue(container, "subscriptionShared", Boolean.class));
			assertEquals(3, TestUtils.getPropertyValue(container, "concurrentConsumers"));
			assertTrue(TestUtils.getPropertyValue(container, "pubSubDomain", Boolean.class));
		}

	}

	@IntegrationTest({ "jms.sessionTransacted = true", "jms.clientId = client", "jms.destination = topic",
			"jms.subscriptionName = subName", "jms.subscriptionDurable = true",
			"jms.subscriptionShared = false", "spring.jms.listener.acknowledgeMode = AUTO",
			"spring.jms.listener.concurrency = 3",
			"spring.jms.listener.maxConcurrency = 4" })
	public static class PropertiesPopulated2Tests extends JmsSourceTests {

		@Test
		public void test() throws Exception {
			AbstractMessageListenerContainer container = TestUtils.getPropertyValue(this.endpoint, "listenerContainer",
					AbstractMessageListenerContainer.class);
			assertThat(container, instanceOf(DefaultMessageListenerContainer.class));
			assertEquals(Session.AUTO_ACKNOWLEDGE, TestUtils.getPropertyValue(container, "sessionAcknowledgeMode"));
			assertTrue(TestUtils.getPropertyValue(container, "sessionTransacted", Boolean.class));
			assertEquals("client", TestUtils.getPropertyValue(container, "clientId"));
			assertEquals("topic", TestUtils.getPropertyValue(container, "destination"));
			assertTrue(TestUtils.getPropertyValue(container, "subscriptionDurable", Boolean.class));
			assertEquals("subName", TestUtils.getPropertyValue(container, "subscriptionName"));
			assertFalse(TestUtils.getPropertyValue(container, "subscriptionShared", Boolean.class));
			assertEquals(3, TestUtils.getPropertyValue(container, "concurrentConsumers"));
			assertEquals(4, TestUtils.getPropertyValue(container, "maxConcurrentConsumers"));
			assertTrue(TestUtils.getPropertyValue(container, "pubSubDomain", Boolean.class));
		}

	}

	@IntegrationTest({ "jms.sessionTransacted = true", "jms.destination = jmssource.test.queue",
		"jms.messageSelector = JMSCorrelationId=foo",
		"jms.subscriptionDurable = false", "jms.subscriptionShared = false",
		"spring.jms.listener.acknowledgeMode = AUTO",
		"spring.jms.listener.concurrency = 3",
		"spring.jms.listener.maxConcurrency = 4",
		"spring.jms.pubSubDomain = false" })
	public static class PropertiesPopulated3Tests extends JmsSourceTests {

		@Autowired
		private JmsTemplate template;

		@Test
		public void test() throws Exception {
			AbstractMessageListenerContainer container = TestUtils.getPropertyValue(this.endpoint, "listenerContainer",
					AbstractMessageListenerContainer.class);
			assertThat(container, instanceOf(DefaultMessageListenerContainer.class));
			assertEquals(Session.AUTO_ACKNOWLEDGE, TestUtils.getPropertyValue(container, "sessionAcknowledgeMode"));
			assertTrue(TestUtils.getPropertyValue(container, "sessionTransacted", Boolean.class));
			assertEquals("jmssource.test.queue", TestUtils.getPropertyValue(container, "destination"));
			assertEquals("JMSCorrelationId=foo", TestUtils.getPropertyValue(container, "messageSelector"));
			assertFalse(TestUtils.getPropertyValue(container, "subscriptionDurable", Boolean.class));
			assertFalse(TestUtils.getPropertyValue(container, "subscriptionShared", Boolean.class));
			assertEquals(3, TestUtils.getPropertyValue(container, "concurrentConsumers"));
			assertEquals(4, TestUtils.getPropertyValue(container, "maxConcurrentConsumers"));
			assertFalse(TestUtils.getPropertyValue(container, "pubSubDomain", Boolean.class));

			template.convertAndSend("jmssource.test.queue", "Hello, world!");
			Message<?> received = messageCollector.forChannel(channels.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertEquals("Hello, world!", received.getPayload());
		}

	}

	@SpringBootApplication
	public static class JmsSourceApplication {

	}

}
