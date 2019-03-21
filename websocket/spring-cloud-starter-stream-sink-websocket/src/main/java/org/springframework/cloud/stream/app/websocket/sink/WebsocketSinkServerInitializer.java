/*
 * Copyright 2014-2016 the original author or authors.
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
 *
 */

package org.springframework.cloud.stream.app.websocket.sink;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.TraceRepository;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Does some basic initialization and setup.
 *
 * <ul>
 * <li>Configure the {@link SslContext} based on {@link WebsocketSinkProperties#ssl}</li>
 * <li>add the {@link WebsocketSinkServerHandler} to the underlying {@link ChannelPipeline}</li>
 * </ul>
 *
 * @author Oliver Moser
 * @author Gary Russell
 */
public class WebsocketSinkServerInitializer extends ChannelInitializer<SocketChannel> {

	public static final int MAX_CONTENT_LENGTH = 65536;

	private final TraceRepository traceRepository;

	@Autowired
	private WebsocketSinkProperties properties;

	@Value("${endpoints.websocketsinktrace.enabled:false}")
	private boolean traceEnabled;

	public WebsocketSinkServerInitializer(TraceRepository traceRepository) {
		this.traceRepository = traceRepository;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		final SslContext sslCtx = configureSslContext();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}

		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
		pipeline.addLast(new WebsocketSinkServerHandler(traceRepository, properties, traceEnabled));
	}

	private SslContext configureSslContext() throws CertificateException, SSLException {
		if (properties.isSsl()) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
		} else {
			return null;
		}
	}
}
