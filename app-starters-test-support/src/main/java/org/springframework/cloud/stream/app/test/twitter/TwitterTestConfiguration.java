/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.app.test.twitter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * Adds a mock {@link TwitterTemplate} to enable contextLoads() tests
 * to run successfully.
 *
 * @author Gary Russell
 *
 */
@Configuration
public class TwitterTestConfiguration {

	@SuppressWarnings("unchecked")
	@Bean
	@ConditionalOnClass(TwitterTemplate.class)
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
				ResponseExtractor<?> extractor = invocation.getArgumentAt(3, ResponseExtractor.class);
				extractor.extractData(response);
				return null;
			}

		}).when(restTemplate).execute(any(URI.class), any(HttpMethod.class), any(RequestCallback.class),
				any(ResponseExtractor.class));
		when(mockTemplate.getRestTemplate()).thenReturn(restTemplate);
		return mockTemplate;
	}

}
