/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.throughput.sink;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ThroughputSinkTests.ThroughputSinkApplication.class)
@WebAppConfiguration
@DirtiesContext
public class ThroughputSinkTests {

	@Autowired
	private Sink sink;

	@Autowired
	private ThroughputSinkConfiguration configuration;

	@Test
	public void testSink() throws Exception {
		assertNotNull(this.sink.input());
		this.sink.input().send(new GenericMessage<>("foo"));
		Log logger = spy(TestUtils.getPropertyValue(this.configuration, "logger", Log.class));
		new DirectFieldAccessor(this.configuration).setPropertyValue("logger", logger);
		final CountDownLatch latch = new CountDownLatch(1);
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.callRealMethod();
				latch.countDown();
				return null;
			}

		}).when(logger).info(anyString());
		assertTrue(latch.await(10, TimeUnit.SECONDS));
	}

	@SpringBootApplication
	public static class ThroughputSinkApplication {

	}

}
