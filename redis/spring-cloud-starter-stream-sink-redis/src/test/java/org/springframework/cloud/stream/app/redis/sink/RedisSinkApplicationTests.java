/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.redis.sink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.junit.redis.RedisTestSupport;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Mark Pollack
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RedisSinkApplicationTests.RedisSinkApplication.class)
@WebIntegrationTest({"server.port:0","redis.key=foo"})
@DirtiesContext
public class RedisSinkApplicationTests {

	@Rule
	public RedisTestSupport redisAvailableRule = new RedisTestSupport();

	@Autowired
	private Sink sink;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Test
	public void contextLoads() {
		assertNotNull(this.sink.input());
	}

	@Test
	public void testWithKey() throws Exception{
		//Setup
		String key = "foo";
		StringRedisTemplate redisTemplate = createStringRedisTemplate(redisConnectionFactory);
		redisTemplate.delete(key);

		RedisList<String> redisList = new DefaultRedisList<String>(key, redisTemplate);
		List<String> list = new ArrayList<String>();
		list.add("Manny");
		list.add("Moe");
		list.add("Jack");

		//Execute
		Message<List<String>> message = new GenericMessage<List<String>>(list);
		sink.input().send(message);

		//Assert
		assertEquals(3, redisList.size());
		assertEquals("Manny", redisList.get(0));
		assertEquals("Moe", redisList.get(1));
		assertEquals("Jack", redisList.get(2));

		//Cleanup
		redisTemplate.delete(key);
	}

	protected StringRedisTemplate createStringRedisTemplate(RedisConnectionFactory connectionFactory) {
		StringRedisTemplate redisTemplate = new StringRedisTemplate();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@SpringBootApplication
	static class RedisSinkApplication {

		public static void main(String[] args) {
			SpringApplication.run(RedisSinkApplication.class, args);
		}
	}
}
