/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.sftp.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileUtils;
import org.springframework.cloud.stream.app.sftp.SftpSessionFactoryConfiguration;
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
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.dsl.sftp.SftpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.util.StringUtils;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({SftpSourceProperties.class, FileConsumerProperties.class})
@Import({TriggerConfiguration.class, SftpSessionFactoryConfiguration.class, TriggerPropertiesMaxMessagesDefaultUnlimited.class})
public class SftpSourceConfiguration {

	@Autowired
	@Qualifier("defaultPoller")
	PollerMetadata defaultPoller;

	@Autowired
	Source source;

	@Bean
	public IntegrationFlow sftpInboundFlow(SessionFactory<LsEntry> sftpSessionFactory, SftpSourceProperties properties,
			FileConsumerProperties fileConsumerProperties) {
		SftpInboundChannelAdapterSpec messageSourceBuilder = Sftp.inboundAdapter(sftpSessionFactory)
				.preserveTimestamp(properties.isPreserveTimestamp())
				.remoteDirectory(properties.getRemoteDir())
				.remoteFileSeparator(properties.getRemoteFileSeparator())
				.localDirectory(properties.getLocalDir())
				.autoCreateLocalDirectory(properties.isAutoCreateLocalDir())
				.temporaryFileSuffix(properties.getTmpFileSuffix())
				.deleteRemoteFiles(properties.isDeleteRemoteFiles());

		if (StringUtils.hasText(properties.getFilenamePattern())) {
			messageSourceBuilder.filter(new SftpSimplePatternFileListFilter(properties.getFilenamePattern()));
		}
		else if (properties.getFilenameRegex() != null) {
			messageSourceBuilder
					.filter(new SftpRegexPatternFileListFilter(properties.getFilenameRegex()));
		}

		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(messageSourceBuilder
				, new Consumer<SourcePollingChannelAdapterSpec>() {

			@Override
			public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
				sourcePollingChannelAdapterSpec
						.poller(SftpSourceConfiguration.this.defaultPoller);
			}

		});

		return FileUtils.enhanceFlowForReadingMode(flowBuilder, fileConsumerProperties)
				.channel(this.source.output())
				.get();
	}

}
