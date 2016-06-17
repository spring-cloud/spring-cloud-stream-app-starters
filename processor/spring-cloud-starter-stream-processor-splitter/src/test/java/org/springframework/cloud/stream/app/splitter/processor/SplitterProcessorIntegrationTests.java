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

package org.springframework.cloud.stream.app.splitter.processor;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
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
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.file.splitter.FileSplitter.FileMarker;
import org.springframework.integration.file.splitter.FileSplitter.FileMarker.Mark;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasSequenceNumber;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasSequenceSize;
import static org.springframework.integration.test.matcher.PayloadMatcher.hasPayload;

/**
 * Integration Tests for the Splitter Processor.
 *
 * @author Gary Russell
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SplitterProcessorIntegrationTests.SplitterProcessorApplication.class)
@DirtiesContext
public abstract class SplitterProcessorIntegrationTests {

	@Autowired
	@Bindings(SplitterProcessorConfiguration.class)
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@Autowired
	protected AbstractMessageSplitter splitter;

	@Autowired
	@Qualifier("splitterProcessorConfiguration.splitterHandler.splitter")
	protected EventDrivenConsumer consumer;

	/**
	 * Validates that the module loads with default properties.
	 */
	public static class UsingNothingIntegrationTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(DefaultMessageSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			this.channels.input().send(new GenericMessage<>(Arrays.asList("hello", "world")));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("hello")));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("world")));
		}
	}

	@IntegrationTest("splitter.delimiters = ,")
	public static class WithDelimitersTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(DefaultMessageSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			this.channels.input().send(new GenericMessage<>("hello,world"));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("hello")));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("world")));
		}
	}

	@IntegrationTest({ "splitter.fileMarkers = false", "splitter.charset = UTF-8", "splitter.applySequence = false" })
	public static class FromFileTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() throws Exception {
			assertThat(this.splitter, instanceOf(FileSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "splitter.proc.test");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write("hello\nworld\n".getBytes());
			fos.close();
			this.channels.input().send(new GenericMessage<>(file));
			Message<?> m = this.collector.forChannel(this.channels.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(m);
			assertNull((m.getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_NUMBER)));
			assertThat(m, hasPayload("hello"));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("world")));
			file.delete();
		}
	}

	@IntegrationTest({ "splitter.fileMarkers = true", "splitter.charset = UTF-8", "splitter.markersJson = false" })
	public static class FromFileWithMarkersTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() throws Exception {
			assertThat(this.splitter, instanceOf(FileSplitter.class));
			assertSame(this.splitter, TestUtils.getPropertyValue(this.consumer, "handler"));
			File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "splitter.proc.test");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write("hello\nworld\n".getBytes());
			fos.close();
			this.channels.input().send(new GenericMessage<>(file));
			Message<?> m = this.collector.forChannel(this.channels.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(m);
			assertThat(m.getPayload(), instanceOf(FileMarker.class));
			assertThat((FileMarker) m.getPayload(), hasMark(Mark.START));
			assertThat(m, allOf(hasSequenceNumber(1), hasSequenceSize(0)));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("hello")));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("world")));
			assertThat(this.collector.forChannel(this.channels.output()),
					receivesPayloadThat(allOf(
							instanceOf(FileMarker.class),
				 			hasMark(Mark.END))));
			file.delete();
		}
	}

	@IntegrationTest("splitter.expression=payload.split(',')")
	public static class UsingExpressionIntegrationTests extends SplitterProcessorIntegrationTests {

		@Test
		public void test() {
			assertThat(this.splitter, instanceOf(ExpressionEvaluatingSplitter.class));
			this.channels.input().send(new GenericMessage<>("hello,world"));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("hello")));
			assertThat(this.collector.forChannel(this.channels.output()), receivesPayloadThat(is("world")));
		}
	}

	private static MarkerMatcher hasMark(Mark expected) {
		return new MarkerMatcher(expected);
	}

	private static final class MarkerMatcher extends DiagnosingMatcher<FileMarker> {

		private final Mark expected;

		private MarkerMatcher(Mark expected) {
			this.expected = expected;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("the mark to be ").appendText(this.expected.name());
		}

		@Override
		protected boolean matches(Object item, Description mismatchDescription) {
			Mark mark = ((FileMarker) item).getMark();
			boolean match = this.expected.compareTo(mark) == 0;
			if (!match) {
				mismatchDescription.appendText("is ").appendText(mark.name());
			}
			return match;
		}

	}

	@SpringBootApplication
	public static class SplitterProcessorApplication {
	}

}
