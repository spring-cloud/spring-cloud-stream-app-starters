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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.integration.aws.outbound.S3MessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.SetObjectAclRequest;
import com.amazonaws.services.s3.transfer.PersistableTransfer;
import com.amazonaws.services.s3.transfer.internal.S3ProgressListener;
import com.amazonaws.services.s3.transfer.internal.S3ProgressPublisher;
import com.amazonaws.util.Md5Utils;
import com.amazonaws.util.StringInputStream;

/**
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@DirtiesContext
@TestPropertySource(properties = {
		"cloud.aws.stack.auto=false",
		"cloud.aws.credentials.accessKey=" + AmazonS3SinkMockTests.AWS_ACCESS_KEY,
		"cloud.aws.credentials.secretKey=" + AmazonS3SinkMockTests.AWS_SECRET_KEY,
		"cloud.aws.region.static=" + AmazonS3SinkMockTests.AWS_REGION,
		"s3.bucket=" + AmazonS3SinkMockTests.S3_BUCKET })
public abstract class AmazonS3SinkMockTests {

	protected static final String AWS_ACCESS_KEY = "test.accessKey";

	protected static final String AWS_SECRET_KEY = "test.secretKey";

	protected static final String AWS_REGION = "us-gov-west-1";

	protected static final String S3_BUCKET = "S3_BUCKET";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Autowired
	private AmazonS3Client amazonS3;

	@Autowired
	protected S3MessageHandler s3MessageHandler;

	@Autowired
	protected Sink channels;

	@Autowired
	protected CountDownLatch aclLatch;

	@Autowired
	protected CountDownLatch transferCompletedLatch;

	@Before
	public void setupTest() {
		Object transferManager = TestUtils.getPropertyValue(this.s3MessageHandler, "transferManager");

		AmazonS3 amazonS3 = spy(this.amazonS3);

		willAnswer(new Answer<PutObjectResult>() {

			@Override
			public PutObjectResult answer(InvocationOnMock invocation) throws Throwable {
				return new PutObjectResult();
			}

		}).given(amazonS3)
				.putObject(any(PutObjectRequest.class));

		willAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				aclLatch.countDown();
				return null;
			}

		}).given(amazonS3)
				.setObjectAcl(any(SetObjectAclRequest.class));

		new DirectFieldAccessor(transferManager).setPropertyValue("s3", amazonS3);
	}

	public abstract void test() throws Exception;

	@IntegrationTest({ "s3.acl=PublicReadWrite" })
	public static class AmazonS3UploadFileTests extends AmazonS3SinkMockTests {

		@Test
		@Override
		public void test() throws Exception {
			AmazonS3 amazonS3Client = TestUtils.getPropertyValue(this.s3MessageHandler, "transferManager.s3",
					AmazonS3.class);

			File file = this.temporaryFolder.newFile("foo.mp3");
			Message<?> message = MessageBuilder.withPayload(file)
					.build();

			this.channels.input().send(message);

			ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
					ArgumentCaptor.forClass(PutObjectRequest.class);
			verify(amazonS3Client, atLeastOnce()).putObject(putObjectRequestArgumentCaptor.capture());

			PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
			assertThat(putObjectRequest.getBucketName(), equalTo(S3_BUCKET));
			assertThat(putObjectRequest.getKey(), equalTo("foo.mp3"));
			assertNull(putObjectRequest.getFile());
			assertNotNull(putObjectRequest.getInputStream());

			ObjectMetadata metadata = putObjectRequest.getMetadata();
			assertThat(metadata.getContentMD5(), equalTo(Md5Utils.md5AsBase64(file)));
			assertThat(metadata.getContentLength(), equalTo(0L));
			assertThat(metadata.getContentType(), equalTo("audio/mpeg"));

			ProgressListener listener = putObjectRequest.getGeneralProgressListener();
			S3ProgressPublisher.publishProgress(listener, ProgressEventType.TRANSFER_COMPLETED_EVENT);

			assertTrue(this.transferCompletedLatch.await(10, TimeUnit.SECONDS));
			assertTrue(this.aclLatch.await(10, TimeUnit.SECONDS));

			ArgumentCaptor<SetObjectAclRequest> setObjectAclRequestArgumentCaptor =
					ArgumentCaptor.forClass(SetObjectAclRequest.class);
			verify(amazonS3Client).setObjectAcl(setObjectAclRequestArgumentCaptor.capture());

			SetObjectAclRequest setObjectAclRequest = setObjectAclRequestArgumentCaptor.getValue();

			assertThat(setObjectAclRequest.getBucketName(), equalTo(S3_BUCKET));
			assertThat(setObjectAclRequest.getKey(), equalTo("foo.mp3"));
			assertNull(setObjectAclRequest.getAcl());
			assertThat(setObjectAclRequest.getCannedAcl(), equalTo(CannedAccessControlList.PublicReadWrite));
		}

	}

	@IntegrationTest({ "s3.key-expression=headers.key" })
	public static class AmazonS3UploadInputStreamTests extends AmazonS3SinkMockTests {


		@Test
		@Override
		public void test() throws Exception {
			AmazonS3 amazonS3Client = TestUtils.getPropertyValue(this.s3MessageHandler, "transferManager.s3",
					AmazonS3.class);

			InputStream payload = new StringInputStream("a");
			Message<?> message = MessageBuilder.withPayload(payload)
					.setHeader("key", "myInputStream")
					.build();

			this.channels.input().send(message);

			ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor =
					ArgumentCaptor.forClass(PutObjectRequest.class);
			verify(amazonS3Client, atLeastOnce()).putObject(putObjectRequestArgumentCaptor.capture());

			PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
			assertThat(putObjectRequest.getBucketName(), equalTo(S3_BUCKET));
			assertThat(putObjectRequest.getKey(), equalTo("myInputStream"));
			assertNull(putObjectRequest.getFile());
			assertNotNull(putObjectRequest.getInputStream());

			ObjectMetadata metadata = putObjectRequest.getMetadata();
			assertThat(metadata.getContentMD5(), equalTo(Md5Utils.md5AsBase64(payload)));
			assertThat(metadata.getContentLength(), equalTo(1L));
			assertThat(metadata.getContentType(), equalTo(MediaType.APPLICATION_JSON_VALUE));
			assertThat(metadata.getContentDisposition(), equalTo("test.json"));
		}

	}

	@SpringBootApplication
	public static class S3SinkApplication {

		@Bean
		public CountDownLatch aclLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public CountDownLatch transferCompletedLatch() {
			return new CountDownLatch(1);
		}

		@Bean
		public S3ProgressListener s3ProgressListener() {
			return new S3ProgressListener() {

				@Override
				public void onPersistableTransfer(PersistableTransfer persistableTransfer) {

				}

				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					if (ProgressEventType.TRANSFER_COMPLETED_EVENT.equals(progressEvent.getEventType())) {
						transferCompletedLatch().countDown();
					}
				}

			};
		}

		@Bean
		public S3MessageHandler.UploadMetadataProvider uploadMetadataProvider() {
			return new S3MessageHandler.UploadMetadataProvider() {

				@Override
				public void populateMetadata(ObjectMetadata metadata, Message<?> message) {
					if (message.getPayload() instanceof InputStream) {
						metadata.setContentLength(1);
						metadata.setContentType(MediaType.APPLICATION_JSON_VALUE);
						metadata.setContentDisposition("test.json");
					}
				}

			};
		}
	}

}
