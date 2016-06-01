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
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Adam Zwickey
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MongoDBSourceApplicationTests.MongoSourceApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class MongoDBSourceApplicationTests {

	private static final MongodStarter starter = MongodStarter.getDefaultInstance();
	private MongodExecutable _mongodExe;
	private MongodProcess _mongod;
	private MongoClient _mongo;

	@Autowired
	@Bindings(MongoDBSourceConfiguration.class)
	protected Source source;

	@Autowired
	protected MessageCollector messageCollector;

	@Before
	public void setUp() throws Exception {
		_mongodExe = starter.prepare(new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(12345, Network.localhostIsIPv6()))
				.build());
		_mongod = _mongodExe.start();
		_mongo = new MongoClient("localhost", 12345);

		DB db = _mongo.getDB("test");
		DBCollection col = db.createCollection("testing", new BasicDBObject());
		col.save(new BasicDBObject("testDoc1", new Date()));
		col.save(new BasicDBObject("testDoc2", new Date()));
	}

	@After
	public void tearDown() throws Exception {
		_mongod.stop();
		_mongodExe.stop();
	}

	public static class DefaultPropertiesTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.notNullValue());
			assertEquals(1, messageCollector.forChannel(source.output()).size());
		}
	}

	@IntegrationTest( { "query= { key: invalid }"} )
	public static class InvalidQueryTests extends MongoDBSourceApplicationTests {
		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertThat(received, CoreMatchers.nullValue());
		}
	}

	@SpringBootApplication
	public static class MongoSourceApplication {

	}


}
