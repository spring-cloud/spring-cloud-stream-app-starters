/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.cloud.stream.app.field.value.counter.sink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.analytics.metrics.FieldValueCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.junit.redis.RedisTestSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestFieldValueCounterSinkApplication.class)
@WebIntegrationTest({"server.port:-1", "field-value-counter.fieldName:test"})
@DirtiesContext
public class FieldValueCounterSinkWithDefaultsTests {

	@Rule
	public RedisTestSupport redisTestSupport = new RedisTestSupport();

	// default field value counter name
	private static final String FVC_NAME = "field-value-counter";

	@Autowired
	@Bindings(FieldValueCounterSinkConfiguration.class)
	private Sink sink;

	@Autowired
	private FieldValueCounterRepository fieldValueCounterRepository;

	@Before
	@After
	public void clear() {
		fieldValueCounterRepository.reset(FVC_NAME);
	}

	@Test
	public void testFieldNameIncrement() {
		assertNotNull(this.sink.input());
		Message<String> message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		message = MessageBuilder.withPayload("{\"test\": \"Hello\"}").build();
		sink.input().send(message);
		message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		assertEquals(2, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("Hi").longValue());
		assertEquals(1, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("Hello").longValue());
	}

}
