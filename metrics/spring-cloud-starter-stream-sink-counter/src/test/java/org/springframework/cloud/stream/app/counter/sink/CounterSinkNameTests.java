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
package org.springframework.cloud.stream.app.counter.sink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@WebIntegrationTest({ "counter.name:simpleCounter" })
public class CounterSinkNameTests extends AbstractCounterSinkTests {

	@Test
	public void testIncrement() throws InterruptedException {
		assertNotNull(this.sink.input());
		Message<String> message = MessageBuilder.withPayload("Hi").build();
		sink.input().send(message);
		Thread.sleep(sleepTime);
		// Note: If the name of the counter does not start with 'counter' or 'metric' the
		// 'counter.' prefix is added
		// by the DefaultCounterService
		assertEquals(1, this.redisMetricRepository.findOne("counter.simpleCounter").getValue().longValue());
	}

}
