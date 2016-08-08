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

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.tcp.AbstractTcpConnectionFactoryProperties;

/**
 * Properties for the TCP client Source.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gary Russell
 */
@ConfigurationProperties("tcp")
public class TcpClientSourceProperties extends AbstractTcpConnectionFactoryProperties {

	/**
	 * The host to which this client will connect.
	 */
	private String host = "localhost";

	/**
	 * The decoder to use when receiving messages.
	 */
	private Encoding decoder = Encoding.CRLF;

	/**
	 * The buffer size used when decoding messages; larger messages will be rejected.
	 */
	private int bufferSize = 2048;

	/**
	 * The charset used when converting from bytes to String.
	 */
	private String charset = "UTF-8";

	/**
	 * Retry interval (in milliseconds) to check the connection and reconnect.
	 */
	private long retryInterval = 60000;

	@NotNull
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@NotNull
	public Encoding getDecoder() {
		return this.decoder;
	}

	public void setDecoder(Encoding decoder) {
		this.decoder = decoder;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public long getRetryInterval() {
		return this.retryInterval;
	}

	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}

}
