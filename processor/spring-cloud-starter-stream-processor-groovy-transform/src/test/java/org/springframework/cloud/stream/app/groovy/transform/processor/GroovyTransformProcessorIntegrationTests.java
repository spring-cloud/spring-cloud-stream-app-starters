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

package org.springframework.cloud.stream.app.groovy.transform.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

/**
 * Integration Tests for the Groovy Transform Processor.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Artem Bilan
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
		classes = GroovyTransformProcessorIntegrationTests.GroovyTransformProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class GroovyTransformProcessorIntegrationTests {

	@Autowired
	@Bindings(GroovyTransformProcessorConfiguration.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@IntegrationTest({"groovy-transformer.script=script.groovy", "groovy-transformer.variables=limit=5\\n foo=\\\\\40WORLD"})
	public static class UsingScriptIntegrationTests extends GroovyTransformProcessorIntegrationTests {

		@Test
		public void test() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello WORLD")));
		}

	}

	@SpringBootApplication
	public static class GroovyTransformProcessorApplication {

	}

}
