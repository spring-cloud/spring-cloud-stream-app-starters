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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A transformer that takes the maven repository coordinates, command line
 * arguments, deployment properties, string payload and datasource configuration
 * for a task and outputs a {@link TaskLaunchRequest} message.
 *
 * @author Glenn Renfro
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(TasklaunchrequestTransformProcessorProperties.class)
public class TasklaunchrequestTransformProcessorConfiguration {

	/**
	 * Pattern used for parsing a String of comma-delimited key=value pairs.
	 */
	private static final Pattern DEPLOYMENT_PROPERTIES_PATTERN = Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");

	/**
	 * Pattern used for parsing a String of command-line arguments.
	 */
	private static final Pattern DEPLOYMENT_PARAMS_PATTERN = Pattern.compile("(\\s(?=([^\\\"']*[\\\"'][^\\\"']*[\\\"'])*[^\\\"']*$))");

	@Autowired
	private TasklaunchrequestTransformProcessorProperties processorProperties;

	@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public Object setupRequest(String message) {
		Map<String, String> properties = new HashMap<String, String>();
		Map<String, String> deploymentProperties = null;
		List<String> commandLineArgs = null;

		if (StringUtils.hasText(processorProperties.getDataSourceUrl())) {
			properties.put("spring_datasource_url", processorProperties.getDataSourceUrl());
		}
		if (StringUtils.hasText(processorProperties.getDataSourceDriverClassName())) {
			properties.put("spring_datasource_driverClassName", processorProperties.getDataSourceDriverClassName());
		}
		if (StringUtils.hasText(processorProperties.getDataSourceUserName())) {
			properties.put("spring_datasource_username", processorProperties.getDataSourceUserName());
		}
		if (StringUtils.hasText(processorProperties.getDataSourcePassword())) {
			properties.put("spring_datasource_password", processorProperties.getDataSourcePassword());
		}
		if (StringUtils.hasLength(processorProperties.getDeploymentProperties())) {
			deploymentProperties = parse(processorProperties.getDeploymentProperties());
		}
		if (StringUtils.hasLength(processorProperties.getCommandLineArguments())) {
			commandLineArgs = parseParams(processorProperties.getCommandLineArguments());
		}

		TaskLaunchRequest request = new TaskLaunchRequest(
				processorProperties.getUri(), commandLineArgs, properties,
				deploymentProperties);

		return request;
	}

	/**
	 * Parses a String comprised of 0 or more comma-delimited key=value pairs where each key has the format:
	 * {@code app.[appname].[key]}.
	 * Values may themselves contain commas, since the split points will be based upon the key pattern.
	 *
	 * @param s the string to parse
	 * @return the Map of parsed key value pairs
	 */
	private Map<String, String> parse(String s) {
		Map<String, String> deploymentProperties = new HashMap<String, String>();

		if (!StringUtils.isEmpty(s)) {
			Matcher matcher = DEPLOYMENT_PROPERTIES_PATTERN.matcher(s);
			int start = 0;
			while (matcher.find()) {
				addKeyValuePairAsProperty(s.substring(start, matcher.start()), deploymentProperties);
				start = matcher.start() + 1;
			}
			addKeyValuePairAsProperty(s.substring(start), deploymentProperties);
		}
		return deploymentProperties;
	}

	/**
	 * Adds a String of format key=value to the provided Map as a key/value pair.
	 *
	 * @param pair       the String representation
	 * @param properties the Map to which the key/value pair should be added
	 */
	private void addKeyValuePairAsProperty(String pair, Map<String, String> properties) {
		int firstEquals = pair.indexOf('=');
		if (firstEquals != -1) {
			properties.put(pair.substring(0, firstEquals).trim(), pair.substring(firstEquals + 1).trim());
		}
	}

	/**
	 * Parses a string of space delimited command line parameters and returns a
	 * list of parameters which doesn't contain any special quoting either for
	 * values or whole parameter.
	 *
	 * @param param string containing a list
	 * @return the list
	 */
	private List<String> parseParams(String param) {
		Assert.hasText(param, "param must not be empty nor null");
		List<String> paramsToUse = new ArrayList<>();
		Matcher regexMatcher = DEPLOYMENT_PARAMS_PATTERN.matcher(param);
		int start = 0;
		while (regexMatcher.find()) {
			String p = removeQuoting(param.substring(start, regexMatcher.start()).trim());
			if (StringUtils.hasText(p)) {
				paramsToUse.add(p);
			}
			start = regexMatcher.start();
		}
		if (param != null && param.length() > 0) {
			String p = removeQuoting(param.substring(start, param.length()).trim());
			if (StringUtils.hasText(p)) {
				paramsToUse.add(p);
			}
		}
		return paramsToUse;
	}

	private static String removeQuoting(String param) {
		param = removeQuote(param, '\'');
		param = removeQuote(param, '"');
		if (StringUtils.hasText(param)) {
			String[] split = param.split("=", 2);
			if (split.length == 2) {
				String value = removeQuote(split[1], '\'');
				value = removeQuote(value, '"');
				param = split[0] + "=" + value;
			}
		}
		return param;
	}

	private static String removeQuote(String param, char c) {
		if (param != null && param.length() > 1) {
			if (param.charAt(0) == c && param.charAt(param.length() - 1) == c) {
				param = param.substring(1, param.length() - 1);
			}
		}
		return param;
	}
}
