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

package org.springframework.cloud.stream.app.tasklaunchrequest.transform.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.StringUtils;

/**
 * A transform that takes the maven repository coordinates, command line
 * arguments, deployment properties, string payload and datasource configuration
 * for a task and outputs a {@link TaskLaunchRequest} message.
 *
 * @author Glenn Renfro
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TasklaunchrequestTransformProcessorProperties.class)
public class TasklaunchrequestTransformProcessorConfiguration {

	@Autowired
	private TasklaunchrequestTransformProcessorProperties processorProperties;

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object setupRequest(String message) {
		Map<String, String> properties = new HashMap<String,String>();
		Map<String, String> deploymentProperties = null;
		List<String> commandLineArgs = null;

		if(StringUtils.hasText(processorProperties.getDataSourceUrl())){
			properties.put("spring_datasource_url",processorProperties.getDataSourceUrl());
		}
		if(StringUtils.hasText(processorProperties.getDataSourceDriverClassName())){
			properties.put("spring_datasource_driverClassName",processorProperties.getDataSourceDriverClassName());
		}
		if(StringUtils.hasText(processorProperties.getDataSourceUserName())){
			properties.put("spring_datasource_username",processorProperties.getDataSourceUserName());
		}
		if(StringUtils.hasText(processorProperties.getDataSourcePassword())){
			properties.put("spring_datasource_password",processorProperties.getDataSourcePassword());
		}
		if(StringUtils.hasLength(processorProperties.getDeploymentProperties())) {
			deploymentProperties = ParsingUtils.parse(processorProperties.getDeploymentProperties());
		}
		if(StringUtils.hasLength(processorProperties.getCommandLineArguments())) {
			commandLineArgs = ParsingUtils.parseParams(processorProperties.getCommandLineArguments());
		}

		properties.put("payload", message);

		TaskLaunchRequest request = new TaskLaunchRequest(
				processorProperties.getUri(), commandLineArgs, properties,
				deploymentProperties);

		return new GenericMessage<TaskLaunchRequest>(request);
	}
}
