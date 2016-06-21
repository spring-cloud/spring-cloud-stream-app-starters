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
package org.springframework.cloud.stream.app.triggertask.source.arguments;

/**
 * Provides the ability to transform previously configured command line args as needed
 * before the sending of a {@link org.springframework.cloud.task.launcher.TaskLaunchRequest}
 *
 * @author Michael Minella
 */
public interface CommandLineArgumentTransformer {

	/**
	 * Transforms the configured command line arguments.
	 *
	 * @param commandLineArgs the configured command line arguments
	 * @return a String to be passed to the task as it's command line arguments
	 */
	String transform(String commandLineArgs);
}
