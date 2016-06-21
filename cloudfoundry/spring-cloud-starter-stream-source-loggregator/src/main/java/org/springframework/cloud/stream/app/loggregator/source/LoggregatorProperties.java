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
package org.springframework.cloud.stream.app.loggregator.source;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *  Configuration properties for the Loggregator Source module.
 *
 * @author Josh Long
 * @author Gary Russell
 */
@ConfigurationProperties("loggregator")
public class LoggregatorProperties {

	private String cloudFoundryApi;

	private String cloudFoundryUser;

	private String cloudFoundryPassword;

	private String applicationName;

	public String getCloudFoundryApi() {
		return cloudFoundryApi;
	}

	public void setCloudFoundryApi(String cloudFoundryApi) {
		this.cloudFoundryApi = cloudFoundryApi;
	}

	public String getCloudFoundryUser() {
		return cloudFoundryUser;
	}

	public void setCloudFoundryUser(String cloudFoundryUser) {
		this.cloudFoundryUser = cloudFoundryUser;
	}

	public String getCloudFoundryPassword() {
		return cloudFoundryPassword;
	}

	public void setCloudFoundryPassword(String cloudFoundryPassword) {
		this.cloudFoundryPassword = cloudFoundryPassword;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

}
