/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.app.tcp.client.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

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
import org.springframework.cloud.stream.app.tcp.EncoderDecoderFactoryBean;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayRawSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayStxEtxSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;
import org.springframework.integration.ip.util.TestingUtilities;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;

/**
 * Tests for TcpClient processor.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TcpClientTests.TcpClientApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true, value = { "tcp.host = localhost", "tcp.port = ${tcp.client.test.port}" })
public abstract class TcpClientTests {

	private static TestTCPServer server;

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected AbstractConnectionFactory connectionFactory;

	@BeforeClass
	public static void startup() {
		server = new TestTCPServer();
	}

	@AfterClass
	public static void shutDown() {
		server.shutDown();
	}

	@IntegrationTest({ "tcp.host = foo", "tcp.decoder=LF", "tcp.encoder=NULL", "tcp.bufferSize=1234", "tcp.nio = true",
			"tcp.reverseLookup = true", "tcp.useDirectBuffers = true", "tcp.socketTimeout = 123", "tcp.charset = bar" })
	public static class PropertiesPopulatedTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNioClientConnectionFactory.class));
			assertEquals("foo", this.connectionFactory.getHost());
			assertTrue(this.connectionFactory.getDeserializer() instanceof ByteArrayLfSerializer);
			assertEquals(1234, ((ByteArrayLfSerializer) this.connectionFactory.getDeserializer()).getMaxMessageSize());
			assertTrue(this.connectionFactory.getSerializer() instanceof ByteArraySingleTerminatorSerializer);
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "usingDirectBuffers", Boolean.class));
			assertEquals(123, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
			assertFalse(this.connectionFactory.isSingleUse());
			assertEquals("bar", TestUtils.getPropertyValue(this.connectionFactory, "mapper.charset"));
		}

	}

	public static class CRLFTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayCrLfSerializer(), "foo", "", "\r\n");
		}

	}

	@IntegrationTest({ "tcp.decoder= LF"})
	public static class LFTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLfSerializer(), "bar", "", "\n");
		}

	}

	@IntegrationTest({ "tcp.decoder = NULL" })
	public static class NULLTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArraySingleTerminatorSerializer((byte) 0), "foo", "", "\u0000");
		}

	}

	@IntegrationTest({ "tcp.decoder = STXETX" })
	public static class STXETXTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayStxEtxSerializer(), "\u0002", "foo", "\u0003");
		}

	}

	@IntegrationTest({ "tcp.decoder = L1" })
	public static class L1Tests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(1), "\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = L2" })
	public static class L2Tests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(2), "\u0000\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = L4" })
	public static class L4Tests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayLengthHeaderSerializer(4), "\u0000\u0000\u0000\u0003", "foo", "");
		}

	}

	@IntegrationTest({ "tcp.decoder = RAW" })
	public static class RAWTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			doTest(new ByteArrayRawSerializer(), "", "foo", "");
		}

	}


	/*
	 * Sends a message and asserts it arrives as expected on the other side using
	 * the supplied decoder. Also, verifies the message received from the TCP server.
	 */
	protected void doTest(AbstractByteArraySerializer encoderDecoder, String prefix, String payload, String suffix) throws Exception {
		server.setEncoder(encoderDecoder);
		server.setDecoder(encoderDecoder);
		server.setPrefix(prefix);
		server.setSuffix(suffix);
		Message<String> messageToSend = new GenericMessage<>(payload);
		assertTrue(channels.input().send(messageToSend));
		assertThat(this.messageCollector.forChannel(channels.output()),
				receivesPayloadThat(is((payload + "-received") .getBytes())));
		server.serverSocket.close();
	}

	/**
	 * TCP server that uses the supplied {@link AbstractByteArraySerializer}
	 * to decode the input stream and writes response to the output stream.
	 *
	 */
	private static class TestTCPServer implements Runnable {

		private static final Log logger = LogFactory.getLog(TestTCPServer.class);

		private final ServerSocket serverSocket;

		private final ExecutorService executor;

		private volatile AbstractByteArraySerializer encoder;

		private volatile AbstractByteArraySerializer decoder;

		private volatile boolean stopped;

		private volatile String prefix = "";

		private volatile String suffix = "\r\n";

		public TestTCPServer() {
			ServerSocket serverSocket = null;
			ExecutorService executor = null;
			try {
				serverSocket = ServerSocketFactory.getDefault().createServerSocket(0);
				System.setProperty("tcp.client.test.port", Integer.toString(serverSocket.getLocalPort()));
				executor = Executors.newSingleThreadExecutor();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			this.serverSocket = serverSocket;
			this.executor = executor;
			this.encoder = new ByteArrayCrLfSerializer();
			this.decoder = new ByteArrayCrLfSerializer();
			executor.execute(this);
		}

		public void setEncoder(AbstractByteArraySerializer encoder) {
			this.encoder = encoder;
		}

		public void setDecoder(AbstractByteArraySerializer decoder) {
			this.decoder = decoder;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		@Override
		public void run() {
			while (true) {
				Socket socket = null;
				try {
					logger.info("Server listening on " + this.serverSocket.getLocalPort());
					socket = this.serverSocket.accept();
					String received = "-received";
					while (true) {
						byte[] data = decoder.deserialize(socket.getInputStream());
						encoder.serialize((new String(data) + received + suffix).getBytes(), socket.getOutputStream());
						if (StringUtils.isEmpty(prefix) && StringUtils.isEmpty(suffix)) {
							socket.close();
						}
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
	@Import(TcpClientProcessorConfiguration.class)
	public static class TcpClientApplication {

	}

}
