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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;

/**
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestFieldValueCounterSinkApplication.class)
@WebIntegrationTest({"server.port:-1", "field-value-counter.name:FVCounter", "field-value-counter.fieldName:test"})
@DirtiesContext
public class FieldValueCounterSinkTests {

	@Rule
	public RedisTestSupport redisTestSupport = new RedisTestSupport();

	private static final String FVC_NAME = "FVCounter";

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

	@Test
	public void testFieldNameDecrement() {
		assertNotNull(this.sink.input());
		Message<String> message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		message = MessageBuilder.withPayload("{\"test\": \"Hi\"}").build();
		sink.input().send(message);
		assertEquals(2, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("Hi").longValue());
		this.fieldValueCounterRepository.decrement(FVC_NAME, "Hi", 2);
		assertEquals(0, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("Hi").longValue());
	}

	@Test
	public void testPojoListFieldName() {
		TestPojoList testPojo = new TestPojoList();
		List<String> test = new ArrayList<>();
		test.add("Hi");
		test.add("Hello");
		test.add("Hi");
		testPojo.setTest(test);
		assertNotNull(this.sink.input());
		Message<TestPojoList> message = MessageBuilder.withPayload(testPojo).build();
		sink.input().send(message);
		assertEquals(2, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("Hi").longValue());
	}

	@Test
	public void testPojoTupleFieldName() {
		TestPojoTuple testPojo = new TestPojoTuple();
		testPojo.setTest(TupleBuilder.tuple().of("test1", "Hi"));
		assertNotNull(this.sink.input());
		Message<TestPojoTuple> message1 = MessageBuilder.withPayload(testPojo).build();
		sink.input().send(message1);
		TestPojoTuple testPojo2 = new TestPojoTuple();
		testPojo2.setTest(TupleBuilder.tuple().of("test2", "Hello"));
		Message<TestPojoTuple> message2 = MessageBuilder.withPayload(testPojo2).build();
		sink.input().send(message2);
		TestPojoTuple testPojo3 = new TestPojoTuple();
		testPojo3.setTest(TupleBuilder.tuple().of("test1", "Hi"));
		Message<TestPojoTuple> message3 = MessageBuilder.withPayload(testPojo3).build();
		sink.input().send(message3);
		assertEquals(2, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("{\"test1\":\"Hi\"}").longValue());
		assertEquals(1, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("{\"test2\":\"Hello\"}").longValue());
	}

	@Test
	public void testPojoMapFieldName() {
		TestPojoMap testPojoMap1 = new TestPojoMap();
		Map<String, String> map1 = new HashMap<>();
		map1.put("test1", "Hello");
		testPojoMap1.setTest(map1);
		assertNotNull(this.sink.input());
		Message<TestPojoMap> message1 = MessageBuilder.withPayload(testPojoMap1).build();
		sink.input().send(message1);
		TestPojoMap testPojoMap2 = new TestPojoMap();
		Map<String, String> map2 = new HashMap<>();
		map2.put("test2", "Hi");
		testPojoMap2.setTest(map2);
		Message<TestPojoMap> message2 = MessageBuilder.withPayload(testPojoMap2).build();
		sink.input().send(message2);
		TestPojoMap testPojoMap3 = new TestPojoMap();
		Map<String, String> map3 = new HashMap<>();
		map3.put("test1", "Hello");
		testPojoMap3.setTest(map3);
		Message<TestPojoMap> message3 = MessageBuilder.withPayload(testPojoMap3).build();
		sink.input().send(message3);
		assertEquals(2, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("{test1=Hello}").longValue());
		assertEquals(1, this.fieldValueCounterRepository.findOne(FVC_NAME).getFieldValueCounts().get("{test2=Hi}").longValue());
	}

	private class TestPojoList {

		private List<String> test;

		public List<String> getTest() {
			return this.test;
		}

		public void setTest(List<String> test) {
			this.test = test;
		}

	}

	private class TestPojoTuple {
		private Tuple test;

		public Tuple getTest() {
			return this.test;
		}

		public void setTest(Tuple test) {
			this.test = test;
		}

	}

	private class TestPojoMap {

		private Map<String, String> test;

		public Map<String, String> getTest() {
			return this.test;
		}

		public void setTest(Map<String, String> test) {
			this.test = test;
		}
	}
}
