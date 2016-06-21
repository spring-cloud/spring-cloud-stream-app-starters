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

package org.springframework.cloud.stream.app.tcp.sink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayStxEtxSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for TcpSink.
 *
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TcpSinkTests.TcpSinkApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true, value = { "tcp.host = localhost", "tcp.port = ${tcp.sink.test.port}" })
public abstract class TcpSinkTests {

	private static TestTCPServer server;

	@Autowired
	protected Sink channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected AbstractClientConnectionFactory connectionFactory;

	@Autowired
	protected TcpSinkProperties properties;

	@Autowired
	protected ApplicationContext ctx;

	@BeforeClass
	public static void startup() {
		 server = new TestTCPServer();
	}

	@AfterClass
	public static void shutDown() {
		server.shutDown();
	}

	@IntegrationTest({ "tcp.host = foo", "tcp.nio = true", "tcp.reverseLookup = true",
					"tcp.useDirectBuffers = true", "tcp.socketTimeout = 123", "tcp.close = true", "tcp.charset = bar" })
	public static class PropertiesPopulatedTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNioClientConnectionFactory.class));
			assertEquals("foo", this.connectionFactory.getHost());
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "usingDirectBuffers", Boolean.class));
			assertEquals(123, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
			assertTrue(this.connectionFactory.isSingleUse());
			assertEquals("bar", TestUtils.getPropertyValue(this.connectionFactory, "mapper.charset"));
		}

	}

	@IntegrationTest({ "tcp.host = foo" })
	public static class NotNioTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNetClientConnectionFactory.class));
			assertEquals("foo", this.connectionFactory.getHost());
			assertFalse(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertEquals(120000, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
		}

	}

	public static class CRLFTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayCrLfSerializer());
		}

	}

	@IntegrationTest({ "tcp.encoder = LF" })
	public static class LFTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLfSerializer());
		}

	}

	@IntegrationTest({ "tcp.encoder = NULL" })
	public static class NULLTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArraySingleTerminatorSerializer((byte) 0));
		}

	}

	@IntegrationTest({ "tcp.encoder = STXETX" })
	public static class STXETXTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayStxEtxSerializer());
		}

	}

	@IntegrationTest({ "tcp.encoder = L1" })
	public static class L1Tests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(1));
		}

	}

	@IntegrationTest({ "tcp.encoder = L2" })
	public static class L2Tests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(2));
		}

	}

	@IntegrationTest({ "tcp.encoder = L4" })
	public static class L4Tests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(4));
		}

	}

	@IntegrationTest({ "tcp.encoder = RAW", "tcp.close = true" })
	public static class RAWTests extends TcpSinkTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayRawSerializer());
		}

	}

	/*
	 * Sends two messages and asserts they arrive as expected on the other side using
	 * the supplied decoder.
	 */
	protected void doTest(AbstractByteArraySerializer decoder) throws Exception {
		server.setDecoder(decoder);
		Message<String> message = new GenericMessage<>("foo");
		assertTrue(channels.input().send(message));
		String received = server.queue.poll(10, TimeUnit.SECONDS);
		assertEquals("foo", received);

		assertTrue(channels.input().send(message));
		received = server.queue.poll(10, TimeUnit.SECONDS);
		assertEquals("foo", received);
	}

	/**
	 * TCP server that uses the supplied {@link AbstractByteArraySerializer}
	 * to decode the input stream and put the resulting message in a queue.
	 *
	 */
	private static class TestTCPServer implements Runnable {

		private static final Log logger = LogFactory.getLog(TestTCPServer.class);

		private final ServerSocket serverSocket;

		private final ExecutorService executor;

		private volatile AbstractByteArraySerializer decoder;

		private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

		private volatile boolean stopped;

		public TestTCPServer() {
			ServerSocket serverSocket = null;
			ExecutorService executor = null;
			try {
				serverSocket = ServerSocketFactory.getDefault().createServerSocket(0);
				System.setProperty("tcp.sink.test.port", Integer.toString(serverSocket.getLocalPort()));
				executor = Executors.newSingleThreadExecutor();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			this.serverSocket = serverSocket;
			this.executor = executor;
			this.decoder = new ByteArrayCrLfSerializer();
			executor.execute(this);
		}

		private void setDecoder(AbstractByteArraySerializer decoder) {
			this.decoder = decoder;
		}

		@Override
		public void run() {
			while (true) {
				Socket socket = null;
				try {
					logger.info("Server listening on " + this.serverSocket.getLocalPort());
					socket = this.serverSocket.accept();
					while (true) {
						byte[] data = decoder.deserialize(socket.getInputStream());
						queue.offer(new String(data));
					}
				}
				catch (SoftEndOfStreamException e) {
					// normal close
				}
				catch (IOException e) {
					try {
						if (socket != null) {
							socket.close();
						}
					}
					catch (IOException e1) {
					}
					logger.error(e.getMessage());
					if (this.stopped) {
						logger.info("Server stopped on " + this.serverSocket.getLocalPort());
						break;
					}
				}
			}
		}

		private void shutDown() {
			try {
				this.stopped = true;
				this.serverSocket.close();
				this.executor.shutdownNow();
			}
			catch (IOException e) {
			}
		}

	}

	@SpringBootApplication
	public static class TcpSinkApplication {

	}

}
