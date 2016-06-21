/*
 * Copyright 2014-2016 the original author or authors.
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
 *
 */

package org.springframework.cloud.stream.app.websocket.sink;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * @author Oliver Moser
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebsocketSinkIntegrationTests.WebsocketSinkApplication.class)
@WebIntegrationTest({
	"server.port:0",
	"websocket.port=0",
	"websocket.path=/some_websocket_path",
	"websocket.logLevel=DEBUG",
	"websocket.threads=2"
})
public class WebsocketSinkIntegrationTests {

	public static final int TIMEOUT = 10000;

	public static final int MESSAGE_COUNT = 100;

	public static final int CLIENT_COUNT = 10;

	@Autowired
	@Bindings(WebsocketSinkConfiguration.class)
	private Sink sink;

	@Autowired
	private WebsocketSinkProperties properties;

	@Autowired
	private WebsocketSinkServer sinkServer;

	@Test
	public void contextLoads() {
		assertNotNull(sink.input());
	}

	@Test
	public void checkCmdlineArgs() {
		assertThat(properties.getPath(), is("/some_websocket_path"));
		assertThat(properties.getPort(), is(0));
		assertThat(properties.getLogLevel(), is("DEBUG"));
		assertThat(properties.getThreads(), is(2));
	}

	@Test(timeout = TIMEOUT)
	public void testMultipleMessageSingleSubscriber() throws Exception {
		WebsocketSinkClientHandler handler = new WebsocketSinkClientHandler("handler_0", MESSAGE_COUNT, TIMEOUT);
		doHandshake(handler);

		List<String> messagesToSend = submitMultipleMessages(MESSAGE_COUNT);
		handler.await();

		assertThat(handler.receivedMessages.size(), is(MESSAGE_COUNT));
		assertThat(handler.receivedMessages, contains(messagesToSend.toArray()));
	}

	@Test(timeout = TIMEOUT)
	public void testSingleMessageMultipleSubscribers() throws Exception {

		// create multiple handlers
		List<WebsocketSinkClientHandler> handlers = createHandlerList(CLIENT_COUNT, 1);

		// submit a single message
		String payload = UUID.randomUUID().toString();
		sink.input().send(MessageBuilder.withPayload(payload).build());

		// await completion on each handler
		for (WebsocketSinkClientHandler handler : handlers) {
			handler.await();
			assertThat(handler.receivedMessages.size(), is(1));
			assertThat(handler.receivedMessages.get(0), is(payload));
		}
	}

	@Test(timeout = TIMEOUT)
	public void testMultipleMessagesMultipleSubscribers() throws Exception {

		// create multiple handlers
		List<WebsocketSinkClientHandler> handlers = createHandlerList(CLIENT_COUNT, MESSAGE_COUNT);

		// submit mulitple  message
		List<String> messagesReceived = submitMultipleMessages(MESSAGE_COUNT);

		// wait on each handle
		for (WebsocketSinkClientHandler handler : handlers) {
			handler.await();
			assertThat(handler.receivedMessages.size(), is(messagesReceived.size()));
			assertThat(handler.receivedMessages, is(messagesReceived));
		}
	}

	//
	//
	// HELPERS
	//
	//

	private WebSocketSession doHandshake(WebsocketSinkClientHandler handler)
			throws InterruptedException, ExecutionException {
		String wsEndpoint = "ws://localhost:" + this.sinkServer.getPort() + this.properties.getPath();
		return new StandardWebSocketClient().doHandshake(handler, wsEndpoint).get();
	}

	private List<String> submitMultipleMessages(int messageCount) {
		List<String> messagesToSend = new ArrayList<>(messageCount);
		for (int i = 0; i < messageCount; i++) {
			String message = "message_" + i;
			messagesToSend.add(message);
			sink.input().send(MessageBuilder.withPayload(message).build());
		}

		return messagesToSend;
	}

	private List<WebsocketSinkClientHandler> createHandlerList(int handlerCount, int messageCount) throws
			InterruptedException,
			ExecutionException {

		List<WebsocketSinkClientHandler> handlers = new ArrayList<>(handlerCount);
		for (int i = 0; i < handlerCount; i++) {
			WebsocketSinkClientHandler handler = new WebsocketSinkClientHandler("handler_" + i, messageCount, TIMEOUT);
			doHandshake(handler);
			handlers.add(handler);
		}
		return handlers;
	}

	/*
	 * simple WebsocketHandler implementation that catches the received messages in a list
	 * for checking the messages in the integration test
	 */
	public static class WebsocketSinkClientHandler extends AbstractWebSocketHandler {

		final List<String> receivedMessages = new ArrayList<>();

		final int waitMessageCount;

		final CountDownLatch latch;

		final long timeout;

		final String id;

		public WebsocketSinkClientHandler(String id, int waitMessageCount, long timeout) {
			this.id = id;
			this.waitMessageCount = waitMessageCount;
			this.latch = new CountDownLatch(waitMessageCount);
			this.timeout = timeout;
		}

		@Override
		public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
			receivedMessages.add(message.getPayload());
			latch.countDown();
		}

		public void await() throws InterruptedException {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		}
	}

	@SpringBootApplication
	public static class WebsocketSinkApplication {

	}

}
