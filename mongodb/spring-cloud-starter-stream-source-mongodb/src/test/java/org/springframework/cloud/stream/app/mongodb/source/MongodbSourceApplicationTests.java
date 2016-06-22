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

package org.springframework.cloud.stream.app.mongodb.source;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * @author Adam Zwickey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MongodbSourceApplicationTests.MongoSourceApplication.class})
@TestPropertySource(properties = {"spring.data.mongodb.port=0"})
@DirtiesContext
public abstract class MongodbSourceApplicationTests {

	@Autowired
	private MongoClient mongo;

	@Autowired
	@Bindings(MongodbSourceConfiguration.class)
	protected Source source;

	@Autowired
	protected MessageCollector messageCollector;

	@Before
	public void setUp() {
		DB db = mongo.getDB("test");
		DBCollection col = db.createCollection("testing", new BasicDBObject());
		col.save(new BasicDBObject("greeting", "hello"));
		col.save(new BasicDBObject("greeting", "hola"));
	}

	@After
	public void tearDown() throws Exception {

	}

	@IntegrationTest(value = {"mongodb.collection=testing", "trigger.fixedDelay=1"})
	public static class DefaultTests extends MongodbSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
		}
	}

	@IntegrationTest(value = {"mongodb.collection=testing",  "mongodb.query={ 'greeting': 'hola' }", "trigger.fixedDelay=1"})
	public static class ValidQueryTests extends MongodbSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat((String)received.getPayload(), CoreMatchers.containsString("hola"));
		}
	}

	@IntegrationTest(value = {"mongodb.collection=testing", "mongodb.query={ 'greeting': 'bogus' }", "trigger.fixedDelay=1"})
	public static class InvalidQueryTests extends MongodbSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.nullValue());
		}
	}

	@IntegrationTest(value = {"mongodb.collection=testing", "trigger.fixedDelay=1", "mongodb.split=false"})
	public static class NoSplitTests extends MongodbSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat(received.getPayload(), Matchers.instanceOf(List.class));
			assertThat(received.getPayload().toString(), CoreMatchers.containsString("hola"));
			assertThat(received.getPayload().toString(), CoreMatchers.containsString("hello"));
		}
	}

	@IntegrationTest(value = {"mongodb.collection=testing", "trigger.fixedDelay=100"})
	public static class MongoTriggerTests extends MongodbSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(1, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.nullValue());
		}
	}

	@SpringBootApplication
	public static class MongoSourceApplication {

	}


}
