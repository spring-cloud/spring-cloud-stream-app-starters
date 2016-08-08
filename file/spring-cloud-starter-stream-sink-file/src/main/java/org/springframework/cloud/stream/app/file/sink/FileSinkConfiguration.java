/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.file.sink;

import java.io.File;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.DefaultFileNameGenerator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.FileWritingMessageHandler;

/**
 * Creates a {@link FileWritingMessageHandler} bean and registers it as a
 * Service Activator that consumes messages from the Sink input channel.
 *
 * @author Mark Fisher
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(FileSinkProperties.class)
public class FileSinkConfiguration {

	@Bean
	@ServiceActivator(inputChannel = Sink.INPUT)
	public FileWritingMessageHandler fileWritingMessageHandler(FileNameGenerator fileNameGenerator, FileSinkProperties properties) {
		FileWritingMessageHandler handler = (properties.getDirectoryExpression() != null)
				? new FileWritingMessageHandler(properties.getDirectoryExpression())
				: new FileWritingMessageHandler(new File(properties.getDirectory()));
		handler.setAutoCreateDirectory(true);
		handler.setAppendNewLine(!properties.isBinary());
		handler.setCharset(properties.getCharset());
		handler.setExpectReply(false);
		handler.setFileExistsMode(properties.getMode());
		handler.setFileNameGenerator(fileNameGenerator);
		return handler;
	}

	@Bean
	public FileNameGenerator fileNameGenerator(FileSinkProperties properties) {
		DefaultFileNameGenerator fileNameGenerator = new DefaultFileNameGenerator();
		fileNameGenerator.setExpression(properties.getNameExpression());
		return fileNameGenerator;
	}
}
