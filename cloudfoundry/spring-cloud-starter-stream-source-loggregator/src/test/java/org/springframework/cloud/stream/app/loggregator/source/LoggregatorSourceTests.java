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
package org.springframework.cloud.stream.app.loggregator.source;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.app.loggregator.source.LoggregatorSourceTests.LoggregatorSourceApplication;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test the Spring Cloud Dataflow CloudFoundry Loggregator source
 *
 * @author Josh Long
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LoggregatorSourceApplication.class)
@EnableConfigurationProperties(LoggregatorProperties.class)
@IntegrationTest({"loggregator.applicationName=foo", "loggregator.cloudFoundryUser=bar",
		"loggregator.cloudFoundryPassword=baz", "loggregator.cloudFoundryApi=qux"})
@DirtiesContext
public class LoggregatorSourceTests {

	@Autowired
	private Source channels;

	@Autowired
	private MessageCollector messageCollector;

	@Test
	public void testLogReceipt() throws Exception {

		Message<?> received = this.messageCollector.forChannel(this.channels.output()).poll(10, TimeUnit.SECONDS);
		assertNotNull(received);
		assertThat((String) received.getPayload(), is(equalTo("hello")));
		assertThat(
				(String) received.getHeaders()
						.get(LoggregatorMessageSource.LoggregatorHeaders.APPLICATION_ID.asHeader()),
				is(equalTo("foo")));
		assertThat((Date) received.getHeaders().get(LoggregatorMessageSource.LoggregatorHeaders.TIMESTAMP.asHeader()),
				is(equalTo(new Date(1))));
		assertThat(
				(ApplicationLog.MessageType) received.getHeaders()
						.get(LoggregatorMessageSource.LoggregatorHeaders.MESSAGE_TYPE.asHeader()),
				is(equalTo(ApplicationLog.MessageType.STDOUT)));
		assertThat(
				(String) received.getHeaders().get(LoggregatorMessageSource.LoggregatorHeaders.SOURCE_NAME.asHeader()),
				is(equalTo("srcN")));
		assertThat((String) received.getHeaders().get(LoggregatorMessageSource.LoggregatorHeaders.SOURCE_ID.asHeader()),
				is(equalTo("srcID")));
	}

	@SpringBootApplication
	public static class LoggregatorSourceApplication {

		@Bean
		public CloudFoundryClient cloudFoundryClient() {
			CloudFoundryClient mockClient = mock(CloudFoundryClient.class);
			doAnswer(new Answer<Void>() {

				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					ApplicationLogListener listener = invocation.getArgumentAt(1, ApplicationLogListener.class);
					listener.onMessage(new ApplicationLog("foo", "hello", new Date(1),
							ApplicationLog.MessageType.STDOUT, "srcN", "srcID"));
					listener.onComplete();
					return null;
				}

			}).when(mockClient).streamLogs(eq("foo"), any(ApplicationLogListener.class));
			return mockClient;
		}

	}

}
