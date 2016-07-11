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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.annotation.PollableSource;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultOne;
import org.springframework.cloud.stream.app.triggertask.source.arguments.CommandLineArgumentTransformer;
import org.springframework.cloud.stream.app.triggertask.source.arguments.PassThroughCommandLineArgumentTransformer;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * Trigger Task source module.
 *
 * @author Glenn Renfro
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({TaskPayloadProperties.class, TriggerPropertiesMaxMessagesDefaultOne.class})
@Import(TriggerConfiguration.class)
public class TriggertaskSourceConfiguration {

	/**
	 * Pattern used for parsing a String of comma-delimited key=value pairs.
	 */
	private static final Pattern PROPERTIES_PATTERN = Pattern.compile("(,\\s*[^=]+=)|(,[^=]+=)");

	/**
	 * Pattern used for parsing a String of command-line arguments.
	 */
	private static final Pattern DEPLOYMENT_PARAMS_PATTERN =
			Pattern.compile("(\\s(?=([^\\\"']*[\\\"'][^\\\"']*[\\\"'])*[^\\\"']*$))");

	@Autowired
	private TaskPayloadProperties taskPayloadProperties;

	private CommandLineArgumentTransformer commandLineArgumentTransformer =
			new PassThroughCommandLineArgumentTransformer();

	@PollableSource
	public Object triggerTaskSource() {

		return new TaskLaunchRequest(taskPayloadProperties.getUri(),
				parseParams(
						commandLineArgumentTransformer.transform(
								taskPayloadProperties.getCommandLineArgs())),
				parseProperties(taskPayloadProperties.getEnvironmentProperties()),
				parseProperties(taskPayloadProperties.getDeploymentProperties()));
	}

	/**
	 * @param commandLineArgumentTransformer a custom {@link CommandLineArgumentTransformer}
	 * 		defaults to {@link PassThroughCommandLineArgumentTransformer}
	 */
	public void setCommandLineArgumentTransformer(CommandLineArgumentTransformer commandLineArgumentTransformer) {
		this.commandLineArgumentTransformer = commandLineArgumentTransformer;
	}

	/**
	 * Parses a String comprised of 0 or more comma-delimited key=value pairs.
	 *
	 * @param s the string to parse
	 * @return the Map of parsed key value pairs
	 */
	public static Map<String, String> parseProperties(String s) {
		Map<String, String> properties = new HashMap<String, String>();
		if (!StringUtils.isEmpty(s)) {
			Matcher matcher = PROPERTIES_PATTERN.matcher(s);
			int start = 0;
			while (matcher.find()) {
				addKeyValuePairAsProperty(s.substring(start, matcher.start()), properties);
				start = matcher.start() + 1;
			}
			addKeyValuePairAsProperty(s.substring(start), properties);
		}
		return properties;
	}

	/**
	 * Adds a String of format key=value to the provided Map as a key/value pair.
	 *
	 * @param pair the String representation
	 * @param properties the Map to which the key/value pair should be added
	 */
	private static void addKeyValuePairAsProperty(String pair, Map<String, String> properties) {
		int firstEquals = pair.indexOf('=');
		if (firstEquals != -1) {
			// todo: should key only be a "flag" as in: put(key, true)?
			properties.put(pair.substring(0, firstEquals).trim(), pair.substring(firstEquals + 1).trim());
		}
	}

	/**
	 * Parses a list of command line parameters and returns a list of parameters
	 * which doesn't contain any special quoting either for values or whole parameter.
	 *
	 * @param param the params
	 * @return the list
	 */
	private static List<String> parseParams(String param) {
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
