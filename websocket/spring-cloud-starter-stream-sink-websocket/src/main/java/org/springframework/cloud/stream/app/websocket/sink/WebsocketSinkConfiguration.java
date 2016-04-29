
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

import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.InMemoryTraceRepository;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.websocket.sink.actuator.WebsocketSinkTraceEndpoint;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author Oliver Moser
 * @author Gary Russell
 */
@Configuration
@EnableConfigurationProperties(WebsocketSinkProperties.class)
@EnableBinding(Sink.class)
public class WebsocketSinkConfiguration {

	private static final Log logger = LogFactory.getLog(WebsocketSinkConfiguration.class);

	@Value("${endpoints.websocketsinktrace.enabled:false}")
	private boolean traceEndpointEnabled;

	private final TraceRepository websocketTraceRepository = new InMemoryTraceRepository();

	@PostConstruct
	public void init() throws InterruptedException, CertificateException, SSLException {
		server().run();
	}

	@Bean
	public WebsocketSinkServer server() {
		return new WebsocketSinkServer();
	}

	@Bean
	public WebsocketSinkServerInitializer initializer() {
		return new WebsocketSinkServerInitializer(websocketTraceRepository);
	}

	@Bean
	@ConditionalOnProperty(value = "endpoints.websocketsinktrace.enabled", havingValue = "true")
	public WebsocketSinkTraceEndpoint websocketTraceEndpoint() {
		return new WebsocketSinkTraceEndpoint(websocketTraceRepository);
	}

	@ServiceActivator(inputChannel = Sink.INPUT)
	public void websocketSink(Message<?> message) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Handling message: %s", message));
		}

		SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(message);
		headers.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
		String messagePayload = message.getPayload().toString();
		for (Channel channel : WebsocketSinkServer.channels) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Writing message %s to channel %s", messagePayload, channel.localAddress()));
			}

			channel.write(new TextWebSocketFrame(messagePayload));
			channel.flush();
		}

		if (traceEndpointEnabled) {
			addMessageToTraceRepository(message);
		}
	}

	private void addMessageToTraceRepository(Message<?> message) {
		Map<String, Object> trace = new LinkedHashMap<>();
		trace.put("type", "text");
		trace.put("direction", "out");
		trace.put("id", message.getHeaders().getId());
		trace.put("payload", message.getPayload().toString());
		websocketTraceRepository.add(trace);

	}

}
