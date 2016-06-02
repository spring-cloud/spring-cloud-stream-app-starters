/*
 * Copyright (c) 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License") ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.mongodb.source;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Adam Zwickey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { MongoDBSourceApplicationTests.MongoSourceApplication.class, EmbeddedMongoAutoConfiguration.class})
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class MongoDBSourceApplicationTests {

	@Autowired
	private MongoClient mongo;

	@Autowired
	@Bindings(MongoDBSourceConfiguration.class)
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

	@IntegrationTest(value = {"collection=testing", "fixedDelay=1", "maxRowsPerPoll=2"})
	public static class DefaultTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
		}
	}

	@IntegrationTest(value = {"collection=testing", "fixedDelay=1", "query={ 'greeting': 'hola' }", "maxRowsPerPoll=2"})
	public static class ValidQueryTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat((String)received.getPayload(), CoreMatchers.containsString("hola"));
		}
	}

	@IntegrationTest(value = {"collection=testing", "fixedDelay=1", "query={ 'greeting': 'bogus' }", "maxRowsPerPoll=2"})
	public static class InvalidQueryTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.nullValue());
		}
	}

	@IntegrationTest(value = {"collection=testing", "fixedDelay=1", "maxRowsPerPoll=2", "split=false"})
	public static class NoSplitTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(2, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertThat(received.getPayload(), Matchers.instanceOf(List.class));
			assertThat(received.getPayload().toString(), CoreMatchers.containsString("hola"));
			assertThat(received.getPayload().toString(), CoreMatchers.containsString("hello"));
		}
	}

	@SpringBootApplication
	public static class MongoSourceApplication {

	}


}
