/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.file.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileUtils;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.file.FileInboundChannelAdapterSpec;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Creates a {@link FileReadingMessageSource} bean and registers it as a
 * Inbound Channel Adapter that sends messages to the Source output channel.
 *
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@Import(TriggerConfiguration.class)
@EnableConfigurationProperties({FileSourceProperties.class, FileConsumerProperties.class})
public class FileSourceConfiguration {

	@Autowired
	private FileSourceProperties properties;

	@Autowired
	private FileConsumerProperties fileConsumerProperties;

	@Autowired
	@Qualifier("defaultPoller")
	PollerMetadata defaultPoller;

	@Autowired
	Source source;

	@Bean
	public TriggerProperties triggerProperties() {
		return new TriggerPropertiesMaxMessagesDefaultUnlimited();
	}


	@Bean
	public IntegrationFlow fileSourceFlow() {
		FileInboundChannelAdapterSpec messageSourceSpec = Files.inboundAdapter(new File(this.properties.getDirectory()));

		if (StringUtils.hasText(this.properties.getFilenamePattern())) {
			messageSourceSpec.patternFilter(this.properties.getFilenamePattern());
		} else if (this.properties.getFilenameRegex() != null) {
			messageSourceSpec.regexFilter(this.properties.getFilenameRegex().pattern());
		}

		if (this.properties.isPreventDuplicates()) {
			messageSourceSpec.preventDuplicates();
		}

		IntegrationFlowBuilder flowBuilder = IntegrationFlows
				.from(messageSourceSpec,
						new Consumer<SourcePollingChannelAdapterSpec>() {

							@Override
							public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
								sourcePollingChannelAdapterSpec
										.poller(defaultPoller);
							}

						});
		return FileUtils.enhanceFlowForReadingMode(flowBuilder, this.fileConsumerProperties)
				.channel(source.output())
				.get();
	}

}
