/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.httpclient.processor;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

/**
 * Tests for Http Client Processor.
 *
 * @author Eric Bottard
 * @author Waldemar Hummer
 * @author Mark Fisher
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {HttpClientProcessorTests.HttpClientProcessorApplication.class,
	HttpClientProcessorTests.AdditionalController.class})
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class HttpClientProcessorTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;


	@IntegrationTest(
			value = {"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/greet'"})
	public static class TestRequestGET extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(is("Hello World")));
		}

	}

	@IntegrationTest(
			value = "httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/' + payload")
	public static class TestRequestGETWithUrlExpressionUsingMessage extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("greet"));
			assertThat(messageCollector.forChannel(channels.output()), receivesPayloadThat(containsString("Hello")));
		}

	}

	@IntegrationTest(
			value = {
					"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/greet'",
					"httpclient.body={\"foo\":\"bar\"}",
					"httpclient.httpMethod=POST"})
	public static class TestRequestPOST extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(Matchers.allOf(
							containsString("foo"), containsString("bar"))));
		}

	}

	@IntegrationTest(
			value = {
					"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/greet'",
					"httpclient.httpMethod=POST"})
	public static class TestRequestPOSTWithBodyExpression extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("{\"foo\":\"bar\"}"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(Matchers.allOf(containsString("Hello"),
							containsString("foo"), containsString("bar"))));
		}

	}

	@IntegrationTest(
			value = {
					"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/headers'",
					"httpclient.headersExpression={Key1:'value1',Key2:'value2'}"})
	public static class TestRequestWithHeaders extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("..."));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(is("value1 value2")));
		}

	}

	@IntegrationTest(
			value = {
					"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') +'/greet'",
					"httpclient.httpMethod=POST",
					"httpclient.headersExpression={Accept:'application/octet-stream'}",
					"httpclient.expectedResponseType=byte[]"})
	public static class TestRequestWithReturnType extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(Matchers.isA(byte[].class)));
		}

	}

	@IntegrationTest(
			value = {
					"httpclient.urlExpression='http://localhost:' + @environment.getProperty('local.server.port') + '/greet'",
					"httpclient.httpMethod=POST",
					"httpclient.replyExpression=body.substring(3,8)"})
	public static class TestRequestWithResultExtractor extends HttpClientProcessorTests {

		@Test
		public void testRequest() {
			channels.input().send(new GenericMessage<Object>("hi"));
			assertThat(messageCollector.forChannel(channels.output()),
					receivesPayloadThat(is("lo hi")));
		}

	}

	@RestController
	public static class AdditionalController {

		@RequestMapping("/greet")
		public String greet(@RequestBody(required = false) String who) {
			if (who == null) {
				who = "World";
			}
			return "Hello " + who;
		}

		@RequestMapping("/headers")
		public String headers(@RequestHeader("Key1") String key1, @RequestHeader("Key2") String key2) {
			return key1 + " " + key2;
		}

	}

	@SpringBootApplication
	public static class HttpClientProcessorApplication {

	}

}
