/*
 * Copyright 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.stream.app.triggertask.source;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.util.Assert;

/**
 * The properties used for configuring the TaskLaunchRequest.
 *
 * @author Glenn Renfro
 */
@ConfigurationProperties("triggertask")
public class TaskPayloadProperties {

	/**
	 * Space delimited key=value pairs to be used as commandline variables for the task.
	 */
	private String commandLineArgs = "";

	/**
	 * The uri to the task artifact.
	 */
	private String uri = "";

	/**
	 * Comma delimited key=value pairs to be used as environmentProperties for the task.
	 */
	private String environmentProperties = "";

	/**
	 * Comma delimited key=value pairs to be used as deploymentProperties for the task.
	 */
	private String deploymentProperties = "";

	public String getCommandLineArgs() {
		return commandLineArgs;
	}

	public void setCommandLineArgs(String commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		Assert.hasText(uri, "uri must not be null nor empty");
		this.uri = uri;
	}

	public String getEnvironmentProperties() {
		return environmentProperties;
	}

	public void setEnvironmentProperties(String environmentProperties) {
		this.environmentProperties = environmentProperties;
	}

	public String getDeploymentProperties() {
		return deploymentProperties;
	}

	public void setDeploymentProperties(String deploymentProperties) {
		this.deploymentProperties = deploymentProperties;
	}

}
