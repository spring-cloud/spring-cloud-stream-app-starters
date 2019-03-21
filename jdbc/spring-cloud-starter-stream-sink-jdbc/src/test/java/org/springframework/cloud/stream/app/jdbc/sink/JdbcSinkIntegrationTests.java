/*
 * Copyright 2015 the original author or authors.
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.tuple.Tuple;
import org.springframework.tuple.TupleBuilder;

/**
 * Integration Tests for JdbcSink. Uses hsqldb as a (real) embedded DB.
 *
 * @author Eric Bottard
 * @author Thomas Risberg
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {JdbcSinkIntegrationTests.JdbcSinkApplication.class, EmbeddedDataSourceConfiguration.class})
@IntegrationTest({"server.port=-1"})
@DirtiesContext
public abstract class JdbcSinkIntegrationTests {

	@Autowired
	@Bindings(JdbcSinkConfiguration.class)
	protected Sink channels;

	@Autowired
	protected JdbcOperations jdbcOperations;

	@IntegrationTest
	public static class DefaultBehavior extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			String result = jdbcOperations.queryForObject("select payload from messages", String.class);
			Assert.assertThat(result, is("hello42"));
		}
	}

	@IntegrationTest(value = "jdbc.columns=a,b")
	public static class SimpleMappingTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	// annotation below relies on java.util.Properties so backslash needs to be doubled
	@IntegrationTest(value = "jdbc.columns=a: a.substring(0\\\\, 4), b: b + 624")
	public static class SpELTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload expected = new Payload("hell", 666);
			Payload result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(expected));
		}
	}

	@IntegrationTest(value = {"jdbc.columns=a,b"})
	public static class VaryingInsertTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload a = new Payload("hello", 42);
			Payload b = new Payload("world", 12);
			Payload c = new Payload("bonjour", null);
			Payload d = new Payload(null, 22);
			channels.input().send(MessageBuilder.withPayload(a).build());
			channels.input().send(MessageBuilder.withPayload(b).build());
			channels.input().send(MessageBuilder.withPayload(c).build());
			channels.input().send(MessageBuilder.withPayload(d).build());
			List<Payload> result = jdbcOperations.query("select a, b from messages", new BeanPropertyRowMapper<>(Payload.class));
			Assert.assertThat(result, containsInAnyOrder(
					samePropertyValuesAs(a),
					samePropertyValuesAs(b),
					samePropertyValuesAs(c),
					samePropertyValuesAs(d)
			));
		}
	}

	@IntegrationTest(value = {"jdbc.tableName=no_script", "jdbc.initialize=true", "jdbc.columns=a,b"})
	public static class ImplicitTableCreationTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from no_script", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	@IntegrationTest(value = {"jdbc.tableName=foobar", "jdbc.initialize=classpath:explicit-script.sql", "jdbc.columns=a,b"})
	public static class ExlicitTableCreationTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Payload sent = new Payload("hello", 42);
			channels.input().send(MessageBuilder.withPayload(sent).build());
			Payload result = jdbcOperations.query("select a, b from foobar", new BeanPropertyRowMapper<>(Payload.class)).get(0);
			Assert.assertThat(result, Matchers.samePropertyValuesAs(sent));
		}
	}

	@IntegrationTest(value = {"jdbc.columns=a,b"})
	public static class MapPayloadInsertTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			NamedParameterJdbcOperations namedParameterJdbcOperations = new NamedParameterJdbcTemplate(jdbcOperations);
			Map<String, Object> mapA = new HashMap<>();
			mapA.put("a", "hello1");
			mapA.put("b", 42);
			Map<String, Object> mapB = new HashMap<>();
			mapB.put("a", "hello2");
			mapB.put("b", null);
			Map<String, Object> mapC = new HashMap<>();
			mapC.put("a", "hello3");
			channels.input().send(MessageBuilder.withPayload(mapA).build());
			channels.input().send(MessageBuilder.withPayload(mapB).build());
			channels.input().send(MessageBuilder.withPayload(mapC).build());
			Assert.assertThat(namedParameterJdbcOperations.queryForObject(
					"select count(*) from messages where a = :a and b = :b", mapA, Integer.class), is(1));
			Assert.assertThat(namedParameterJdbcOperations.queryForObject(
					"select count(*) from messages where a = :a and b IS NULL", mapB, Integer.class), is(1));
			Assert.assertThat(namedParameterJdbcOperations.queryForObject(
					"select count(*) from messages where a = :a and b IS NULL", mapC, Integer.class), is(1));
		}
	}

	@IntegrationTest(value = {"jdbc.columns=a,b"})
	public static class TuplePayloadInsertTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			Tuple tupleA = TupleBuilder.tuple().of("a", "hello1", "b", 42);
			Tuple tupleB = TupleBuilder.tuple().of("a", "hello2", "b", null);
			Tuple tupleC = TupleBuilder.tuple().of("a", "hello3");
			channels.input().send(MessageBuilder.withPayload(tupleA).build());
			channels.input().send(MessageBuilder.withPayload(tupleB).build());
			channels.input().send(MessageBuilder.withPayload(tupleC).build());
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b = ?",
					Integer.class, tupleA.getString("a"), tupleA.getInt("b")), is(1));
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b IS NULL",
					Integer.class, tupleB.getString("a")), is(1));
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b IS NULL",
					Integer.class, tupleC.getString("a")), is(1));
		}
	}

	@IntegrationTest(value = {"jdbc.columns=a,b"})
	public static class JsonStringPayloadInsertTests extends JdbcSinkIntegrationTests {

		@Test
		public void testInsertion() {
			String stringA = "{\"a\": \"hello1\", \"b\": 42}";
			String stringB = "{\"a\": \"hello2\", \"b\": null}";
			String stringC = "{\"a\": \"hello3\"}";
			channels.input().send(MessageBuilder.withPayload(stringA).build());
			channels.input().send(MessageBuilder.withPayload(stringB).build());
			channels.input().send(MessageBuilder.withPayload(stringC).build());
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b = ?",
					Integer.class, "hello1", 42), is(1));
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b IS NULL",
					Integer.class, "hello2"), is(1));
			Assert.assertThat(jdbcOperations.queryForObject(
					"select count(*) from messages where a = ? and b IS NULL",
					Integer.class, "hello3"), is(1));
		}
	}

	public static class Payload {
		private String a;

		private Integer b;

		public Payload() {
		}

		public Payload(String a, Integer b) {
			this.a = a;
			this.b = b;
		}

		public String getA() {
			return a;
		}

		public Integer getB() {
			return b;
		}

		public void setA(String a) {
			this.a = a;
		}

		public void setB(Integer b) {
			this.b = b;
		}

		@Override
		public String toString() {
			return a + b;
		}
	}

	@SpringBootApplication
	public static class JdbcSinkApplication {

	}


}