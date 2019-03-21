/*
 * Copyright 2015-2016 the original author or authors.
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
 */

package org.springframework.cloud.stream.app.tcp;

/**
 * Base class for TCP connection factory properties.
 *
 * @author Eric Bottard
 * @author Gary Russell
 */
public abstract class AbstractTcpConnectionFactoryProperties {

	public enum Encoding {
		CRLF, LF, NULL, STXETX, RAW, L1, L2, L4;
	}

	/**
	 * The port on which to listen; 0 for the OS to choose a port.
	 */
	private int port = 1234;

	/**
	 * Perform a reverse DNS lookup on the remote IP Address; if false,
	 * just the IP address is included in the message headers.
	 */
	private boolean reverseLookup = false;

	/**
	 * The timeout (ms) before closing the socket when no data is received.
	 */
	private int socketTimeout = 120000;

	/**
	 * Whether or not to use NIO.
	 */
	private boolean nio = false;

	/**
	 * Whether or not to use direct buffers.
	 */
	private boolean useDirectBuffers = false;

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isUseDirectBuffers() {
		return this.useDirectBuffers;
	}

	public void setUseDirectBuffers(boolean useDirectBuffers) {
		this.useDirectBuffers = useDirectBuffers;
	}

	public boolean isNio() {
		return this.nio;
	}

	public void setNio(boolean nio) {
		this.nio = nio;
	}

	public int getSocketTimeout() {
		return this.socketTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public boolean isReverseLookup() {
		return this.reverseLookup;
	}

	public void setReverseLookup(boolean reverseLookup) {
		this.reverseLookup = reverseLookup;
	}

}
