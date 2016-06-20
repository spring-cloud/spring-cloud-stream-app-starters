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

package org.springframework.cloud.stream.app.s3.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.s3.AmazonS3Configuration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.messaging.MessageHandler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.internal.S3ProgressListener;

/**
 * @author Artem Bilan
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(AmazonS3SinkProperties.class)
@Import(AmazonS3Configuration.class)
public class AmazonS3SinkConfiguration {

	@Autowired(required = false)
	private S3MessageHandler.UploadMetadataProvider uploadMetadataProvider;

	@Autowired(required = false)
	private S3ProgressListener s3ProgressListener;

	@Bean
	@ServiceActivator(inputChannel = Sink.INPUT)
	public MessageHandler amazonS3MessageHandler(AmazonS3 amazonS3, ResourceIdResolver resourceIdResolver,
			AmazonS3SinkProperties s3SinkProperties) {
		S3MessageHandler s3MessageHandler;
		if (s3SinkProperties.getBucket() != null) {
			s3MessageHandler = new S3MessageHandler(amazonS3, s3SinkProperties.getBucket());
		}
		else {
			s3MessageHandler = new S3MessageHandler(amazonS3, s3SinkProperties.getBucketExpression());
		}
		s3MessageHandler.setResourceIdResolver(resourceIdResolver);
		s3MessageHandler.setKeyExpression(s3SinkProperties.getKeyExpression());
		if (s3SinkProperties.getAcl() != null) {
			s3MessageHandler.setObjectAclExpression(new ValueExpression<>(s3SinkProperties.getAcl()));
		}
		else {
			s3MessageHandler.setObjectAclExpression(s3SinkProperties.getAclExpression());
		}
		s3MessageHandler.setUploadMetadataProvider(this.uploadMetadataProvider);
		s3MessageHandler.setProgressListener(this.s3ProgressListener);
		return s3MessageHandler;
	}

}
