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

package org.springframework.cloud.stream.app.tasklaunchrequest.transform.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Glenn Renfro
 */
@ConfigurationProperties
public class TasklaunchrequestTransformProcessorProperties {
	/**
	 * The uri of the artifact to be applied to the TaskLaunchRequest.
	 */
	private String uri;

	/**
	 * The datasource url to be applied to the TaskLaunchRequest.
	 */
	private String dataSourceUrl;

	/**
	 * The datasource driver class name to be applied to the TaskLaunchRequest.
	 */
	private String dataSourceDriverClassName;

	/**
	 * The datasource user name to be applied to the TaskLaunchRequest.
	 */
	private String dataSourceUserName;

	/**
	 * The datasource password to be applied to the TaskLaunchRequest.
	 */
	private String dataSourcePassword;

	/**
	 *  Space delimited list of commandLineArguments to be applied to the
	 *  TaskLaunchRequest.
	 */
	private String commandLineArguments;

	/**
	 * Comma delimited list of deployment properties to be applied to the
	 * TaskLaunchRequest.
	 */
	private String deploymentProperties;


	public String getDataSourceUrl() {
		return dataSourceUrl;
	}

	public void setDataSourceUrl(String dataSourceUrl) {
		this.dataSourceUrl = dataSourceUrl;
	}

	public String getDataSourceDriverClassName() {
		return dataSourceDriverClassName;
	}

	public void setDataSourceDriverClassName(String dataSourceDriverClassName) {
		this.dataSourceDriverClassName = dataSourceDriverClassName;
	}

	public String getDataSourceUserName() {
		return dataSourceUserName;
	}

	public void setDataSourceUserName(String dataSourceUserName) {
		this.dataSourceUserName = dataSourceUserName;
	}

	public String getDataSourcePassword() {
		return dataSourcePassword;
	}

	public void setDataSourcePassword(String dataSourcePassword) {
		this.dataSourcePassword = dataSourcePassword;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getCommandLineArguments() {
		return commandLineArguments;
	}

	public void setCommandLineArguments(String commandLineArguments) {
		this.commandLineArguments = commandLineArguments;
	}

	public String getDeploymentProperties() {
		return deploymentProperties;
	}

	public void setDeploymentProperties(String deploymentProperties) {
		this.deploymentProperties = deploymentProperties;
	}
}
