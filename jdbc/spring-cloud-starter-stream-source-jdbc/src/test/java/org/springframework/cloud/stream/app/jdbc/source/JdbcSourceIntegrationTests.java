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

package org.springframework.cloud.stream.app.jdbc.source;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration Tests for JdbcSource. Uses hsqldb as a (real) embedded DB.
 *
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {JdbcSourceIntegrationTests.JdbcSourceApplication.class,
		EmbeddedDataSourceConfiguration.class})
@DirtiesContext
public abstract class JdbcSourceIntegrationTests {

	@Autowired
	@Bindings(JdbcSourceConfiguration.class)
	protected Source source;

	@Autowired
	protected JdbcOperations jdbcOperations;

	@Autowired
	protected MessageCollector messageCollector;

	@IntegrationTest("query=select id, name from test order by id")
	public static class DefaultBehaviorTests extends JdbcSourceIntegrationTests {

		@Test
		public void testExtraction() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(1L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(2L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(3L, ((Map) received.getPayload()).get("ID"));
		}

	}

	@IntegrationTest(value = {"query=select id, name, tag from test where tag is NULL order by id", "split=false"})
	public static class SelectAllNoSplitTests extends JdbcSourceIntegrationTests {

		@Test
		public void testExtraction() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(List.class));
			assertEquals(3, ((List) received.getPayload()).size());
			assertEquals(1L, ((Map) ((List) received.getPayload()).get(0)).get("ID"));
			assertEquals("John", ((Map) ((List) received.getPayload()).get(2)).get("NAME"));
		}

	}

	@IntegrationTest(value = {"query=select id, name from test order by id", "trigger.fixedDelay=600"})
	public static class SelectAllWithDelayTests extends JdbcSourceIntegrationTests {

		@Test
		public void testExtraction() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			System.out.println(received);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(1L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			System.out.println(received);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(2L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			System.out.println(received);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(3L, ((Map) received.getPayload()).get("ID"));
			// should not wrap around to the beginning since delay is 60
			received = messageCollector.forChannel(source.output()).poll(1, TimeUnit.SECONDS);
			assertNull(received);
		}

	}

	@IntegrationTest(value = {"query=select id, name from test order by id", "trigger.fixedDelay=1"})
	public static class SelectAllWithMinDelayTests extends JdbcSourceIntegrationTests {

		@Test
		public void testExtraction() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(1L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(2L, ((Map) received.getPayload()).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(3L, ((Map) received.getPayload()).get("ID"));
			// should wrap around to the beginning
			received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(Map.class));
			assertEquals(1L, ((Map) received.getPayload()).get("ID"));
		}

	}

	@IntegrationTest(value = {"query=select id, name, tag from test where tag is NULL order by id", "split=false",
			"maxRowsPerPoll=2", "update=update test set tag='1' where id in (:id)"})
	public static class Select2PerPollNoSplitWithUpdateTests extends JdbcSourceIntegrationTests {

		@Test
		public void testExtraction() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(List.class));
			assertEquals(2, ((List) received.getPayload()).size());
			assertEquals(1L, ((Map) ((List) received.getPayload()).get(0)).get("ID"));
			assertEquals(2L, ((Map) ((List) received.getPayload()).get(1)).get("ID"));
			received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(List.class));
			assertEquals(1, ((List) received.getPayload()).size());
			assertEquals(3L, ((Map) ((List) received.getPayload()).get(0)).get("ID"));
		}

	}

	@SpringBootApplication
	public static class JdbcSourceApplication {

	}

}