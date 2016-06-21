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

package org.springframework.cloud.stream.app.scriptable.transform.processor;

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
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

/**
 * Integration Tests for the Script Transform Processor.
 *
 * @author Andy Clement
 * @author Artem Bilan
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
		classes = ScriptableTransformProcessorIntegrationTests.ScriptableTransformProcessorApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class ScriptableTransformProcessorIntegrationTests {

	@Autowired
	@Bindings(ScriptableTransformProcessorConfiguration.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@IntegrationTest({"scriptable-transformer.script=function add(a,b) { return a+b;};add(1,3)", "scriptable-transformer.language=js"})
	public static class JavascriptScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptFunctions() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			// Different Java versions return different types for JavaScript results
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(isOneOf(4, 4L, 4.0)));
		}
	}

	@IntegrationTest({"scriptable-transformer.script=payload+foo", "scriptable-transformer.language=js", "scriptable-transformer.variables=foo=\\\\\40WORLD"})
	public static class JavascriptScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptSimple() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world WORLD")));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=payload*limit", "scriptable-transformer.language=js", "scriptable-transformer.variables=limit=5"})
	public static class JavascriptScriptProperty3Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testJavascriptSimple() {
			channels.input().send(new GenericMessage<Object>(9));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(45.0)));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=payload+foo", "scriptable-transformer.language=groovy", "scriptable-transformer.variables=foo=\\\\\40WORLD"})
	public static class GroovyScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testGroovyBasic() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world WORLD")));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=payload.substring(0, limit as int) + foo", "scriptable-transformer.language=groovy",
			"scriptable-transformer.variables=limit=5\\n foo=\\\\\40WORLD"})
	public static class GroovyScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testGroovyComplex() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello WORLD")));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=return \"\"#{payload.upcase}\"\"", "scriptable-transformer.language=ruby"})
	public static class RubyScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testRubyScript() {
			channels.input().send(new GenericMessage<Object>("hello world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("HELLO WORLD")));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=\"def foo(x)\\n  return x+5\\nend\\nfoo(payload)\\n\"", "scriptable-transformer.language=ruby"})
	public static class RubyScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testRuby() {
			channels.input().send(new GenericMessage<Object>(9));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(14L)));
		}

	}

	// Python not currently supported, problems running it in SCDF

	@IntegrationTest({"scriptable-transformer.script=\"def multiply(x,y):\\n  return x*y\\nanswer = multiply(payload,5)\\n\"",
			"scriptable-transformer.language=python"})
	public static class PythonScriptProperty1Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testPython() {
			channels.input().send(new GenericMessage<Object>(6));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is(30)));
		}

	}

	@IntegrationTest({"scriptable-transformer.script=\"def concat(x,y):\\n  return x+y\\nanswer = concat(\"\"hello \"\",payload)\\n\"",
			"scriptable-transformer.language=python"})
	public static class PythonScriptProperty2Tests extends ScriptableTransformProcessorIntegrationTests {

		@Test
		public void testPython() {
			channels.input().send(new GenericMessage<Object>("world"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
		}

	}

	@SpringBootApplication
	public static class ScriptableTransformProcessorApplication {

	}

}
