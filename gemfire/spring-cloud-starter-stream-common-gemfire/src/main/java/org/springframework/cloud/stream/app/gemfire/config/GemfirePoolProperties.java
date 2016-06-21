/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.gemfire.config;

import java.net.InetSocketAddress;

import org.hibernate.validator.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gemfire client pool configuration properties
 *
 * @author David Turanski
 */
@ConfigurationProperties("gemfire.pool")
public class GemfirePoolProperties {

	public enum ConnectType {locator, server}

	/**
	 * Specifies one or more Gemfire locator or server addresses formatted as [host]:[port].
	 */
	private InetSocketAddress[] hostAddresses = { new InetSocketAddress("localhost", 10334) };

	/**
	 * Specifies connection type: 'server' or 'locator'.
	 */
	private ConnectType connectType = ConnectType.locator;

	/**
	 * Set to true to enable subscriptions for the client pool. Required to sync updates to the client cache.
	 */
	private boolean subscriptionEnabled;

	@NotEmpty
	public InetSocketAddress[] getHostAddresses() {
		return hostAddresses;
	}

	public void setHostAddresses(InetSocketAddress[] hostAddresses) {
		this.hostAddresses = hostAddresses;
	}

	public ConnectType getConnectType() {
		return connectType;
	}

	public void setConnectType(ConnectType connectType) {
		this.connectType = connectType;
	}

	public boolean isSubscriptionEnabled() {
		return subscriptionEnabled;
	}

	public void setSubscriptionEnabled(boolean subscriptionEnabled) {
		this.subscriptionEnabled = subscriptionEnabled;
	}

}
