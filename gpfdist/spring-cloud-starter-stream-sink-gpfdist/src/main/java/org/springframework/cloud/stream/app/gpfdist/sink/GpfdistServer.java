/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gpfdist.sink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.BiFunction;
import reactor.fn.Function;
import reactor.io.buffer.Buffer;
import reactor.io.net.NetStreams;
import reactor.io.net.ReactorChannelHandler;
import reactor.io.net.Spec.HttpServerSpec;
import reactor.io.net.http.HttpChannel;
import reactor.io.net.http.HttpServer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

/**
 * Server implementation around reactor and netty providing endpoint
 * where data can be sent using a gpfdist protocol.
 *
 * @author Janne Valkealahti
 */
public class GpfdistServer {

	private final static Log log = LogFactory.getLog(GpfdistServer.class);

	private final Processor<Buffer, Buffer> processor;
	private final int port;
	private final int flushCount;
	private final int flushTime;
	private final int batchTimeout;
	private final int batchCount;
	private HttpServer<Buffer, Buffer> server;
	private int localPort = -1;

	/**
	 * Instantiates a new gpfdist server.
	 *
	 * @param processor the processor
	 * @param port the port
	 * @param flushCount the flush count
	 * @param flushTime the flush time
	 * @param batchTimeout the batch timeout
	 * @param batchCount the batch count
	 */
	public GpfdistServer(Processor<Buffer, Buffer> processor, int port, int flushCount, int flushTime,
			int batchTimeout, int batchCount) {
		this.processor = processor;
		this.port = port;
		this.flushCount = flushCount;
		this.flushTime = flushTime;
		this.batchTimeout = batchTimeout;
		this.batchCount = batchCount;
	}

	/**
	 * Start a server.
	 *
	 * @return the http server
	 * @throws Exception the exception
	 */
	public synchronized HttpServer<Buffer, Buffer> start() throws Exception {
		if (server == null) {
			server = createProtocolListener();
		}
		return server;
	}

	/**
	 * Stop a server.
	 *
	 * @throws Exception the exception
	 */
	public synchronized void stop() throws Exception {
		if (server != null) {
			server.shutdown().awaitSuccess();
		}
		server = null;
	}

	/**
	 * Gets the local port.
	 *
	 * @return the local port
	 */
	public int getLocalPort() {
		return localPort;
	}

	private HttpServer<Buffer, Buffer> createProtocolListener()
			throws Exception {

		final Stream<Buffer> stream = Streams
		.wrap(processor)
		.window(flushCount, flushTime, TimeUnit.SECONDS)
		.flatMap(new Function<Stream<Buffer>, Publisher<Buffer>>() {

			@Override
			public Publisher<Buffer> apply(Stream<Buffer> t) {

				return t.reduce(new Buffer(), new BiFunction<Buffer, Buffer, Buffer>() {

					@Override
					public Buffer apply(Buffer prev, Buffer next) {
						return prev.append(next);
					}
				});
			}
		})
		.process(RingBufferWorkProcessor.<Buffer>create("gpfdist-sink-worker", 8192, false));

		HttpServer<Buffer, Buffer> httpServer = NetStreams
				.httpServer(new Function<HttpServerSpec<Buffer, Buffer>, HttpServerSpec<Buffer, Buffer>>() {

					@Override
					public HttpServerSpec<Buffer, Buffer> apply(HttpServerSpec<Buffer, Buffer> server) {
						return server
								.codec(new GpfdistCodec())
								.listen(port);
					}
				});

		httpServer.get("/data", new ReactorChannelHandler<Buffer, Buffer, HttpChannel<Buffer,Buffer>>() {

			@Override
			public Publisher<Void> apply(HttpChannel<Buffer, Buffer> request) {
				request.responseHeaders().removeTransferEncodingChunked();
				request.addResponseHeader("Content-type", "text/plain");
				request.addResponseHeader("Expires", "0");
				request.addResponseHeader("X-GPFDIST-VERSION", "Spring Dataflow");
				request.addResponseHeader("X-GP-PROTO", "1");
				request.addResponseHeader("Cache-Control", "no-cache");
				request.addResponseHeader("Connection", "close");

				return request.writeWith(stream
						.take(batchCount)
						.timeout(batchTimeout, TimeUnit.SECONDS, Streams.<Buffer>empty())
						.concatWith(Streams.just(Buffer.wrap(new byte[0]))))
						.capacity(1l);
			}
		});

		httpServer.start().awaitSuccess();
		log.info("Server running using address=[" + httpServer.getListenAddress() + "]");
		localPort = httpServer.getListenAddress().getPort();
		return httpServer;
	}
}
