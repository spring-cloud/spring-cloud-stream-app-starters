/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.sftp;

import org.hibernate.validator.constraints.Range;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.file.remote.AbstractRemoteServerProperties;

/**
 * @author Gary Russell
 */
@ConfigurationProperties("sftp.factory")
public class SftpSessionFactoryProperties extends AbstractRemoteServerProperties {

	/**
	 * The port of the server.
	 */
	private int port = 22;

	/**
	 * Resource location of user's private key.
	 */
	private String privateKey = "";

	/**
	 * Passphrase for user's private key.
	 */
	private String passPhrase = "";

	/**
	 * True to allow an unknown or changed key.
	 */
	private boolean allowUnknownKeys = false;

	/**
	 * A SpEL expression resolving to the location of the known hosts file.
	 */
	private String knownHostsExpression = null;

	@Range(min = 0, max = 65535)
	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPrivateKey() {
		return this.privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassPhrase() {
		return this.passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public boolean isAllowUnknownKeys() {
		return this.allowUnknownKeys;
	}

	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = allowUnknownKeys;
	}

	public String getKnownHostsExpression() {
		return this.knownHostsExpression;
	}

	public void setKnownHostsExpression(String knownHosts) {
		this.knownHostsExpression = knownHosts;
	}

}
