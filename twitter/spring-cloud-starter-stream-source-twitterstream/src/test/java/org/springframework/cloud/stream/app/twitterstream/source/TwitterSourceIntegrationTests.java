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

package org.springframework.cloud.stream.app.twitterstream.source;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.app.twitterstream.source.TwitterSourceIntegrationTests.TestTwitterSourceApplication;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for twitter stream source app.
 *
 * @author Gary Russell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestTwitterSourceApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TwitterSourceIntegrationTests {

	@Autowired
	protected Source twitterSource;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected AtomicReference<URI> uri;

	public static class DefaultPropertiesTests extends TwitterSourceIntegrationTests {

		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(this.twitterSource.output()).poll(10, TimeUnit.SECONDS);
			assertThat(received, notNullValue());
			assertThat((String) received.getPayload(), is(equalTo("foo")));
			assertThat(this.uri.get().toString(), containsString("sample"));
		}

	}

	@IntegrationTest({ "twitter.stream.streamType=FIREHOSE", "twitter.stream.language=english" })
	public static class FireHoseTests extends TwitterSourceIntegrationTests {

		@Test
		public void test() throws InterruptedException {
			Message<?> received = messageCollector.forChannel(this.twitterSource.output()).poll(10, TimeUnit.SECONDS);
			assertThat(received, notNullValue());
			assertThat((String) received.getPayload(), is(equalTo("foo")));
			assertThat(this.uri.get().toString(), containsString("firehose"));
			assertThat(this.uri.get().toString(), containsString("language=english"));
		}

	}

	@SpringBootApplication
	public static class TestTwitterSourceApplication {

		@SuppressWarnings("unchecked")
		@Bean
		public TwitterTemplate twitterTemplate() {
			TwitterTemplate mockTemplate = mock(TwitterTemplate.class);
			RestTemplate restTemplate = mock(RestTemplate.class);
			final ClientHttpResponse response = mock(ClientHttpResponse.class);
			ByteArrayInputStream bais = new ByteArrayInputStream("foo".getBytes());
			try {
				when(response.getBody()).thenReturn(bais);
			}
			catch (IOException e) {
			}
			doAnswer(new Answer<Void>() {

				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					uri().set(invocation.getArgumentAt(0, URI.class));
					ResponseExtractor<?> extractor = invocation.getArgumentAt(3, ResponseExtractor.class);
					extractor.extractData(response);
					return null;
				}

			}).when(restTemplate).execute(any(URI.class), any(HttpMethod.class), any(RequestCallback.class),
					any(ResponseExtractor.class));
			when(mockTemplate.getRestTemplate()).thenReturn(restTemplate);
			return mockTemplate;
		}

		@Bean
		public AtomicReference<URI> uri() {
			return new AtomicReference<URI>();
		}

	}

}
