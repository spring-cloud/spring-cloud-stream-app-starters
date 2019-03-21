/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.cloud.stream.app.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Twitter credentials.
 *
 * @author Ilayaperumal Gopinathan
 * @author Gary Russell
 */
@ConfigurationProperties("twitter.credentials")
//TODO: Move these properties into spring-boot's TwitterProperties prefixed with: `spring.social.twitter`
public class TwitterCredentials {

	/**
	 * Consumer key
	 */
	private String consumerKey;

	/**
	 * Consumer secret
	 */
	private String consumerSecret;

	/**
	 * Access token
	 */
	private String accessToken;

	/**
	 * Access token secret
	 */
	private String accessTokenSecret;

	public String getConsumerKey() {
		return this.consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return this.consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return this.accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
}
