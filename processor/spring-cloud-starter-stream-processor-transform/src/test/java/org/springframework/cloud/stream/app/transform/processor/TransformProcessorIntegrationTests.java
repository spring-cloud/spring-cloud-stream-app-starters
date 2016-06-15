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

package org.springframework.cloud.stream.app.transform.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.config.SpelFunctionFactoryBean;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

/**
 * Integration Tests for the Transform Processor.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Gary Russell
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TransformProcessorIntegrationTests.TransformProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TransformProcessorIntegrationTests {

	@Autowired
	@Bindings(TransformProcessorConfiguration.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	/**
	 * Validates that the module loads with default properties.
	 */
	public static class UsingNothingIntegrationTests extends TransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello")));
		}
	}

	@IntegrationTest("transformer.expression=payload.toUpperCase()")
	public static class UsingExpressionIntegrationTests extends TransformProcessorIntegrationTests {

		@Autowired
		@Qualifier("&jsonPath")
		private SpelFunctionFactoryBean jsonPathSpelFunctionFactoryBean;

		@Autowired
		@Qualifier("&xpath")
		private SpelFunctionFactoryBean xpathSpelFunctionFactoryBean;

		@Test
		public void test() {
			assertNotNull(this.jsonPathSpelFunctionFactoryBean);
			assertNotNull(this.xpathSpelFunctionFactoryBean);
			this.channels.input().send(new GenericMessage<Object>("hello"));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("HELLO")));
		}
	}

	@SpringBootApplication
	public static class TransformProcessorApplication {

	}

}
