/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.stream.app.loggregator.source;

import java.net.MalformedURLException;
import java.net.URI;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;

/**
 *  A source that can be used to read logging messages from Loggregator.
 *
 * @author Josh Long
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({LoggregatorProperties.class})
public class LoggregatorSourceConfiguration {

	@Autowired
	private LoggregatorProperties loggregatorSourceProperties;

	@Bean
	public LoggregatorMessageSource loggregatorMessageSource(
			@Qualifier(Source.OUTPUT) MessageChannel source,
			CloudFoundryClient cloudFoundryClient) {
		return new LoggregatorMessageSource(
				this.loggregatorSourceProperties.getApplicationName(),
				cloudFoundryClient, source);
	}

	@Bean
	public CloudCredentials cloudCredentials() {
		return new CloudCredentials(this.loggregatorSourceProperties.getCloudFoundryUser(),
				this.loggregatorSourceProperties.getCloudFoundryPassword());
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudFoundryClient cloudFoundryClient(CloudCredentials cc) throws MalformedURLException {
		URI uri = URI.create(this.loggregatorSourceProperties.getCloudFoundryApi());
		CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, uri.toURL());
		cloudFoundryClient.login();
		return cloudFoundryClient;
	}

}

