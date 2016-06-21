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

package org.springframework.cloud.stream.app.s3.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.file.FileConsumerProperties;
import org.springframework.cloud.stream.app.file.FileUtils;
import org.springframework.cloud.stream.app.s3.AmazonS3Configuration;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultUnlimited;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.aws.inbound.S3InboundFileSynchronizer;
import org.springframework.integration.aws.inbound.S3InboundFileSynchronizingMessageSource;
import org.springframework.integration.aws.support.S3SessionFactory;
import org.springframework.integration.aws.support.filters.S3RegexPatternFileListFilter;
import org.springframework.integration.aws.support.filters.S3SimplePatternFileListFilter;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.util.StringUtils;

import com.amazonaws.services.s3.AmazonS3;

/**
 * @author Artem Bilan
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({ AmazonS3SourceProperties.class, FileConsumerProperties.class,
		TriggerPropertiesMaxMessagesDefaultUnlimited.class})
@Import({ TriggerConfiguration.class, AmazonS3Configuration.class })
public class AmazonS3SourceConfiguration {

	@Autowired
	private AmazonS3SourceProperties s3SourceProperties;

	@Bean
	public S3InboundFileSynchronizer s3InboundFileSynchronizer(AmazonS3 amazonS3,
			ResourceIdResolver resourceIdResolver) {
		S3SessionFactory s3SessionFactory = new S3SessionFactory(amazonS3, resourceIdResolver);
		S3InboundFileSynchronizer synchronizer = new S3InboundFileSynchronizer(s3SessionFactory);
		synchronizer.setDeleteRemoteFiles(this.s3SourceProperties.isDeleteRemoteFiles());
		synchronizer.setPreserveTimestamp(this.s3SourceProperties.isPreserveTimestamp());
		String remoteDir = this.s3SourceProperties.getRemoteDir();
		synchronizer.setRemoteDirectory(remoteDir);
		synchronizer.setRemoteFileSeparator(this.s3SourceProperties.getRemoteFileSeparator());
		synchronizer.setTemporaryFileSuffix(this.s3SourceProperties.getTmpFileSuffix());

		if (StringUtils.hasText(this.s3SourceProperties.getFilenamePattern())) {
			synchronizer.setFilter(new S3SimplePatternFileListFilter(this.s3SourceProperties.getFilenamePattern()));
		}
		else if (this.s3SourceProperties.getFilenameRegex() != null) {
			synchronizer.setFilter(new S3RegexPatternFileListFilter(this.s3SourceProperties.getFilenameRegex()));
		}

		return synchronizer;
	}

	@Bean
	public IntegrationFlow s3InboundFlow(FileConsumerProperties fileConsumerProperties,
			S3InboundFileSynchronizer s3InboundFileSynchronizer) {
		S3InboundFileSynchronizingMessageSource s3MessageSource =
				new S3InboundFileSynchronizingMessageSource(s3InboundFileSynchronizer);
		s3MessageSource.setLocalDirectory(this.s3SourceProperties.getLocalDir());
		s3MessageSource.setAutoCreateLocalDirectory(this.s3SourceProperties.isAutoCreateLocalDir());

		return FileUtils.enhanceFlowForReadingMode(IntegrationFlows.from(s3MessageSource), fileConsumerProperties)
				.channel(Source.OUTPUT)
				.get();
	}

}
