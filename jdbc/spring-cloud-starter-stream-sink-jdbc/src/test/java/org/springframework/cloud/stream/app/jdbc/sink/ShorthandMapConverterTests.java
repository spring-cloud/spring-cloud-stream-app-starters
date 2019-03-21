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

package org.springframework.cloud.stream.app.jdbc.sink;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for ShorthandMapConverter.
 *
 * @author Eric Bottard
 */
public class ShorthandMapConverterTests {

	private ShorthandMapConverter converter = new ShorthandMapConverter();

	@Test
	public void basic() {
		Map<String, String> result = converter.convert("foo:bar,wizz:bang");
		assertThat(result, hasEntry("foo", "bar"));
		assertThat(result, hasEntry("wizz", "bang"));
		assertThat(result.size(), is(2));
	}

	@Test
	public void singleValue() {
		Map<String, String> result = converter.convert("foo:bar");
		assertThat(result, hasEntry("foo", "bar"));
		assertThat(result.size(), is(1));
	}

	@Test
	public void noValue() {
		Map<String, String> result = converter.convert("");
		assertThat(result.size(), is(0));
	}

	@Test
	public void trimming() {
		Map<String, String> result = converter.convert(" foo :  bar\t");
		assertThat(result, hasEntry("foo", "bar"));
		assertThat(result.size(), is(1));
	}

	@Test
	public void implicitValue() {
		Map<String, String> result = converter.convert("foo");
		assertThat(result, hasEntry("foo", "foo"));
		assertThat(result.size(), is(1));
	}

	@Test
	public void escapes() {
		Map<String, String> result = converter.convert("foo\\:bar:bang,wizz\\,foo:boom\\:splash");
		assertThat(result, hasEntry("foo:bar", "bang"));
		assertThat(result, hasEntry("wizz,foo", "boom:splash"));
		assertThat(result.size(), is(2));
	}

}