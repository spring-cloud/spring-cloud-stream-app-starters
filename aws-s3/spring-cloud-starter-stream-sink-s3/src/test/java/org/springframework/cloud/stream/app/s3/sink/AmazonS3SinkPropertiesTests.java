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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.amazonaws.services.s3.model.CannedAccessControlList;

/**
 * @author Artem Bilan
 */
public class AmazonS3SinkPropertiesTests {

	@Test
	public void s3BucketCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo");
		context.register(Conf.class);
		context.refresh();
		AmazonS3SinkProperties properties = context.getBean(AmazonS3SinkProperties.class);
		assertThat(properties.getBucket(), equalTo("foo"));
		context.close();
	}

	@Test
	public void s3BucketExpressionCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket-expression:headers.bucket");
		context.register(Conf.class);
		context.refresh();
		AmazonS3SinkProperties properties = context.getBean(AmazonS3SinkProperties.class);
		assertThat(properties.getBucketExpression().getExpressionString(), equalTo("headers.bucket"));
		context.close();
	}

	@Test
	public void s3BucketAndBucketExpressionAreMutuallyExclusive() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo", "s3.bucketExpression:headers.bucket");
		context.register(Conf.class);
		try {
			context.refresh();
			fail("BeanCreationException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(BeanCreationException.class));
			assertThat(e.getMessage(), containsString("Exactly one of 'bucket' or 'bucketExpression' must be set"));
		}
	}

	@Test
	public void s3KeyCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo", "s3.keyExpression:'foo'");
		context.register(Conf.class);
		context.refresh();
		AmazonS3SinkProperties properties = context.getBean(AmazonS3SinkProperties.class);
		assertThat(properties.getKeyExpression().getExpressionString(), equalTo("'foo'"));
		context.close();
	}

	@Test
	public void aclCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo", "s3.acl:AuthenticatedRead");
		context.register(Conf.class);
		context.refresh();
		AmazonS3SinkProperties properties = context.getBean(AmazonS3SinkProperties.class);
		assertThat(properties.getAcl(), equalTo(CannedAccessControlList.AuthenticatedRead));
		context.close();
	}

	@Test
	public void aclExpressionCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo", "S3_ACL_EXPRESSION:headers.acl");
		context.register(Conf.class);
		context.refresh();
		AmazonS3SinkProperties properties = context.getBean(AmazonS3SinkProperties.class);
		assertThat(properties.getAclExpression().getExpressionString(), equalTo("headers.acl"));
		context.close();
	}

	@Test
	public void aclAndAclExpressionAreMutuallyExclusive() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "s3.bucket:foo", "s3.acl:private", "s3.acl-expression:'acl'");
		context.register(Conf.class);
		try {
			context.refresh();
			fail("BeanCreationException expected");
		}
		catch (Exception e) {
			assertThat(e, instanceOf(BeanCreationException.class));
			assertThat(e.getMessage(), containsString("Only one of 'acl' or 'aclExpression' must be set"));
		}
	}

	@Configuration
	@EnableConfigurationProperties(AmazonS3SinkProperties.class)
	@Import(SpelExpressionConverterConfiguration.class)
	static class Conf {

	}


}
