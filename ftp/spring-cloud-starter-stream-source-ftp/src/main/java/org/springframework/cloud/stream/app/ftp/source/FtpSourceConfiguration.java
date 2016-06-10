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

package org.springframework.cloud.stream.app.ftp.source;

import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileUtils;
import org.springframework.cloud.stream.app.ftp.FtpSessionFactoryConfiguration;
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
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.dsl.ftp.FtpInboundChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.filters.FtpRegexPatternFileListFilter;
import org.springframework.integration.ftp.filters.FtpSimplePatternFileListFilter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.StringUtils;

/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({FtpSourceProperties.class, FileConsumerProperties.class})
@Import({TriggerConfiguration.class, FtpSessionFactoryConfiguration.class})
public class FtpSourceConfiguration {

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
	public IntegrationFlow ftpInboundFlow(SessionFactory<FTPFile> ftpSessionFactory, FtpSourceProperties properties,
			FileConsumerProperties fileConsumerProperties) {
		FtpInboundChannelAdapterSpec messageSourceBuilder = Ftp.inboundAdapter(ftpSessionFactory)
				.preserveTimestamp(properties.isPreserveTimestamp())
				.remoteDirectory(properties.getRemoteDir())
				.remoteFileSeparator(properties.getRemoteFileSeparator())
				.localDirectory(properties.getLocalDir())
				.autoCreateLocalDirectory(properties.isAutoCreateLocalDir())
				.temporaryFileSuffix(properties.getTmpFileSuffix())
				.deleteRemoteFiles(properties.isDeleteRemoteFiles());

		if (StringUtils.hasText(properties.getFilenamePattern())) {
			messageSourceBuilder.filter(new FtpSimplePatternFileListFilter(properties.getFilenamePattern()));
		}
		else if (properties.getFilenameRegex() != null) {
			messageSourceBuilder
					.filter(new FtpRegexPatternFileListFilter(properties.getFilenameRegex()));
		}

		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(messageSourceBuilder
				, new Consumer<SourcePollingChannelAdapterSpec>() {

			@Override
			public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
				sourcePollingChannelAdapterSpec
						.poller(FtpSourceConfiguration.this.defaultPoller);
			}

		});

		return FileUtils.enhanceFlowForReadingMode(flowBuilder, fileConsumerProperties)
				.channel(this.source.output())
				.get();
	}

}
