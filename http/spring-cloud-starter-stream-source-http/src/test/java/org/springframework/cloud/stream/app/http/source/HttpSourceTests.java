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

package org.springframework.cloud.stream.app.http.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Tests for HttpSourceConfiguration.
 *
 * @author Eric Bottard
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = HttpSourceTests.HttpSourceApplication.class)
public abstract class HttpSourceTests {

	@Autowired
	@Bindings(HttpSourceConfiguration.class)
	protected Source channels;

	@Value("${local.server.port}")
	protected int port;

	@Autowired
	protected MessageCollector messageCollector;

	protected RestTemplate restTemplate = new RestTemplate();

	@WebIntegrationTest(value = "http.pathPattern=/foo", randomPort = true)
	public static class SimpleMappingTests extends HttpSourceTests {

		@Test
		public void testText() {
			ResponseEntity<?> entity = restTemplate.postForEntity("http://localhost:" + port + "/foo", "hello", Object.class);
			assertEquals(HttpStatus.ACCEPTED, entity.getStatusCode());
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
		}

		@Test
		public void testBytes() {
			ResponseEntity<?> entity = restTemplate.postForEntity("http://localhost:" + port + "/foo", "hello".getBytes(), Object.class);
			assertEquals(HttpStatus.ACCEPTED, entity.getStatusCode());
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(is("hello".getBytes())));
		}

		@Test
		public void testJson() throws Exception {
			String json = "{\"foo\":1,\"bar\":true}";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RequestEntity<String> request = new RequestEntity<String>(json, headers, HttpMethod.POST, new URI("http://localhost:" + port + "/foo"));
			ResponseEntity<?> response = restTemplate.exchange(request, Object.class);
			assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
			Message<?> message = messageCollector.forChannel(channels.output()).poll(1, TimeUnit.SECONDS);
			assertEquals(json, message.getPayload());
			assertEquals("application/json", message.getHeaders().get(MessageHeaders.CONTENT_TYPE));
		}
	}

	@WebIntegrationTest(randomPort = true)
	public static class DefaultMappingTests extends HttpSourceTests {

		@Test
		public void testText() {
			ResponseEntity<?> entity = restTemplate.postForEntity("http://localhost:" + port, "hello", Object.class);
			assertEquals(HttpStatus.ACCEPTED, entity.getStatusCode());
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
		}
	}

	@SpringBootApplication
	static class HttpSourceApplication {

		public static void main(String[] args) {
			SpringApplication.run(HttpSourceApplication.class, args);
		}

	}

}
