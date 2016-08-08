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

package org.springframework.cloud.stream.app.tcp.client.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
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
import org.springframework.cloud.stream.app.tcp.client.source.TcpClientSourceConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Source;
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

/**
 * Tests for TcpClient source.
 *
 * @author Ilayaperumal Gopinathan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TcpClientTests.TcpClientApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true, value = { "tcp.host = localhost", "tcp.port = ${tcp.client.test.port}" })
public abstract class TcpClientTests {

	@Autowired
	protected Source source;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected AbstractConnectionFactory connectionFactory;

	private static ServerSocket serverSocket;

	@BeforeClass
	public static void startup() throws Exception {
		serverSocket = ServerSocketFactory.getDefault().createServerSocket(0);
		System.setProperty("tcp.client.test.port", Integer.toString(serverSocket.getLocalPort()));
	}

	@AfterClass
	public static void shutDown() throws Exception {
		serverSocket.close();
	}

	@IntegrationTest({ "tcp.host = foo", "tcp.decoder=LF", "tcp.bufferSize=1234", "tcp.nio = true",
			"tcp.reverseLookup = true", "tcp.useDirectBuffers = true", "tcp.socketTimeout = 123", "tcp.charset = bar" })
	public static class PropertiesPopulatedTests extends TcpClientTests {

		@Test
		public void test() throws Exception {
			assertThat(this.connectionFactory, Matchers.instanceOf(TcpNioClientConnectionFactory.class));
			assertEquals("foo", this.connectionFactory.getHost());
			assertTrue(this.connectionFactory.getDeserializer() instanceof ByteArrayLfSerializer);
			assertEquals(1234, ((ByteArrayLfSerializer) this.connectionFactory.getDeserializer()).getMaxMessageSize());
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "lookupHost", Boolean.class));
			assertTrue(TestUtils.getPropertyValue(this.connectionFactory, "usingDirectBuffers", Boolean.class));
			assertEquals(123, TestUtils.getPropertyValue(this.connectionFactory, "soTimeout"));
			assertFalse(this.connectionFactory.isSingleUse());
			assertEquals("bar", TestUtils.getPropertyValue(this.connectionFactory, "mapper.charset"));
		}

	}


	public static class TestTcpClient extends TcpClientTests {

		@Test
		public void testOutput() throws Exception {
			OutputStream outputStream = serverSocket.accept().getOutputStream();
			outputStream.write("Test1\r\n".getBytes());
			Message<?> message = messageCollector.forChannel(source.output()).poll(5000, TimeUnit.MILLISECONDS);
			assertNotNull(message);
			assertEquals("Test1", new String((byte[]) message.getPayload()));
			outputStream.write("Test2\r\n".getBytes());
			message =  messageCollector.forChannel(source.output()).poll(5000, TimeUnit.MILLISECONDS);
			assertNotNull(message);
			assertEquals("Test2", new String((byte[]) message.getPayload()));
		}

	}

	@SpringBootApplication
	@Import(TcpClientSourceConfiguration.class)
	public static class TcpClientApplication {

	}

}
