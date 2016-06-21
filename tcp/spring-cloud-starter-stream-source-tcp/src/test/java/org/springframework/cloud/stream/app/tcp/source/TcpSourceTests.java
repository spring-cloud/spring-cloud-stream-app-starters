/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.tcp.source;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.net.Socket;

import javax.net.SocketFactory;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for TcpSource.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TcpSourceTests.TcpSourceApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true, value = "tcp.port = 0")
public abstract class TcpSourceTests {

	@Autowired
	protected Source channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected AbstractServerConnectionFactory connectionFactory;

	@Autowired
	protected TcpSourceProperties properties;

	@IntegrationTest({ "tcp.nio = true", "tcp.reverseLookup = true",
					"tcp.useDirectBuffers = true", "tcp.socketTimeout = 123", "tcp.bufferSize = 5" })
	public static class PropertiesPopulatedTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNioServerConnectionFactory.class));
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "usingDirectBuffers", Boolean.class));
			assertEquals(123, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
			assertEquals(5, TestUtils.getPropertyValue(this.connectionFactory, "deserializer.maxMessageSize"));
		}

	}

	public static class NotNioTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNetServerConnectionFactory.class));
			assertFalse(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertEquals(120000, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
			assertEquals(2048, TestUtils.getPropertyValue(this.connectionFactory, "deserializer.maxMessageSize"));
		}

	}

	public static class CRLFTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("", "foo", "\r\n");
		}

	}

	@IntegrationTest({ "tcp.decoder = LF" })
	public static class LFTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("", "foo", "\n");
		}

	}

	@IntegrationTest({ "tcp.decoder = NULL" })
	public static class NULLTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("", "foo", "\u0000");
		}

	}

	@IntegrationTest({ "tcp.decoder = STXETX" })
	public static class STXETXTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("\u0002", "foo", "\u0003");
		}

	}

	@IntegrationTest({ "tcp.decoder = L1" })
	public static class L1Tests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = L2" })
	public static class L2Tests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("\u0000\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = L4" })
	public static class L4Tests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("\u0000\u0000\u0000\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = RAW" })
	public static class RAWTests extends TcpSourceTests {

		@Test
		public void test() throws Exception {
			doTest("", "foo", "");
		}

	}

	/*
	 * Sends two messages with <prefix><payload><suffix> and asserts the
	 * payload is received on the other side.
	 */
	protected void doTest(String prefix, String payload, String suffix) throws Exception {
		int port = getPort();
		Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
		socket.getOutputStream().write((prefix + payload + suffix).getBytes());
		if (prefix.length() == 0 && suffix.length() == 0) {
			socket.close(); // RAW - for the others, close AFTER the messages are decoded.
			socket = SocketFactory.getDefault().createSocket("localhost", port);
		}
		assertThat(this.messageCollector.forChannel(channels.output()), receivesPayloadThat(is(payload.getBytes())));
		socket.getOutputStream().write((prefix + payload + suffix).getBytes());
		if (prefix.length() == 0 && suffix.length() == 0) {
			socket.close(); // RAW - for the others, close AFTER the messages are decoded.
		}
		assertThat(this.messageCollector.forChannel(channels.output()), receivesPayloadThat(is(payload.getBytes())));
		socket.close();
	}

	private int getPort() throws Exception {
		int n = 0;
		while (n++ < 100 && !this.connectionFactory.isListening()) {
			Thread.sleep(100);
		}
		assertTrue("server failed to start listening", this.connectionFactory.isListening());
		int port = this.connectionFactory.getPort();
		assertTrue("server stopped listening", port > 0);
		return port;
	}

	@SpringBootApplication
	public static class TcpSourceApplication {

	}

}
