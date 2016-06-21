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

package org.springframework.cloud.stream.app.aws;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasHeader;
import static org.springframework.integration.test.matcher.PayloadMatcher.hasPayload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.stream.app.s3.source.AmazonS3SourceConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * @author Artem Bilan
 */
public class SpringCloudStreamAwsApplicationsITCase {

	@ClassRule
	public static final AwsIntegrationTestStackRule TEST_STACK_RULE = new AwsIntegrationTestStackRule();

	private ConfigurableApplicationContext applicationContext;

	@After
	public void after() {
		if (this.applicationContext != null) {
			this.applicationContext.close();
		}
	}

	@Test
	public void testS3Source() throws IOException, InterruptedException {
		String bucket = "TestBucket";
		String key = "foo";
		String content = "Spring Cloud Stream AWS S3 Source test";

		this.applicationContext = SpringApplication.run(S3SourceBootConfiguration.class,
				"--s3.remoteDir=" + bucket, "--file.consumer.mode=lines", "--file.consumer.with-markers=false");

		ResourceIdResolver resourceIdResolver = this.applicationContext.getBean(ResourceIdResolver.class);

		AmazonS3 amazonS3 = this.applicationContext.getBean(AmazonS3.class);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(content.length());
		String bucketName = resourceIdResolver.resolveToPhysicalResourceId(bucket);
		amazonS3.putObject(bucketName, key, new ByteArrayInputStream(content.getBytes("UTF-8")), objectMetadata);

		try {
			Source source = this.applicationContext.getBean(Source.class);
			MessageCollector messageCollector = this.applicationContext.getBean(MessageCollector.class);
			Message<?> received = messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received, hasHeader(FileHeaders.FILENAME, key));
			assertThat(received, hasPayload("Spring Cloud Stream AWS S3 Source test"));
		}
		finally {
			amazonS3.deleteObject(bucketName, key);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(AmazonS3SourceConfiguration.class)
	public static class S3SourceBootConfiguration {

	}

}
