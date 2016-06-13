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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.springframework.integration.test.matcher.HeaderMatcher.hasHeader;
import static org.springframework.integration.test.matcher.PayloadMatcher.hasPayload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.aws.support.S3Session;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileCopyUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@DirtiesContext
@TestPropertySource(properties = {
		"cloud.aws.stack.auto=false",
		"cloud.aws.credentials.accessKey=" + AmazonS3SourceMockTests.AWS_ACCESS_KEY,
		"cloud.aws.credentials.secretKey=" + AmazonS3SourceMockTests.AWS_SECRET_KEY,
		"cloud.aws.region.static=" + AmazonS3SourceMockTests.AWS_REGION,
		"trigger.initialDelay=1",
		"s3.remoteDir=" + AmazonS3SourceMockTests.S3_BUCKET })
public abstract class AmazonS3SourceMockTests {

	@ClassRule
	public static final TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

	protected static final String AWS_ACCESS_KEY = "test.accessKey";

	protected static final String AWS_SECRET_KEY = "test.secretKey";

	protected static final String AWS_REGION = "us-gov-west-1";

	protected static final String S3_BUCKET = "S3_BUCKET";

	protected static List<S3Object> S3_OBJECTS;

	@Autowired
	protected SourcePollingChannelAdapter s3ChannelAdapter;

	@Autowired
	protected AmazonS3Client amazonS3;

	@Autowired
	protected Source channels;

	@Autowired
	protected AmazonS3SourceProperties config;

	@Autowired
	protected MessageCollector messageCollector;

	@BeforeClass
	public static void setup() throws IOException {
		File remoteFolder = TEMPORARY_FOLDER.newFolder("remote");

		File aFile = new File(remoteFolder, "1.test");
		FileCopyUtils.copy("Hello".getBytes(), aFile);
		File bFile = new File(remoteFolder, "2.test");
		FileCopyUtils.copy("Bye".getBytes(), bFile);
		File otherFile = new File(remoteFolder, "otherFile");
		FileCopyUtils.copy("Other\nOther2".getBytes(), otherFile);

		S3_OBJECTS = new ArrayList<>();

		for (File file : remoteFolder.listFiles()) {
			S3Object s3Object = new S3Object();
			s3Object.setBucketName(S3_BUCKET);
			s3Object.setKey(file.getName());
			s3Object.setObjectContent(new FileInputStream(file));
			S3_OBJECTS.add(s3Object);
		}

		String localFolder = TEMPORARY_FOLDER.newFolder("local").getAbsolutePath();

		System.setProperty("s3.localDir", localFolder);
	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty("s3.localDir");
	}

	@Before
	public void setupTest() {
		this.s3ChannelAdapter.stop();
		S3Session s3Session = TestUtils.getPropertyValue(this.s3ChannelAdapter,
				"source.synchronizer.remoteFileTemplate.sessionFactory.s3Session", S3Session.class);

		AmazonS3 amazonS3 = spy(this.amazonS3);

		willAnswer(new Answer<ObjectListing>() {

			@Override
			public ObjectListing answer(InvocationOnMock invocation) throws Throwable {
				ObjectListing objectListing = new ObjectListing();
				List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
				for (S3Object s3Object : S3_OBJECTS) {
					S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
					s3ObjectSummary.setBucketName(S3_BUCKET);
					s3ObjectSummary.setKey(s3Object.getKey());
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.DATE, 1);
					s3ObjectSummary.setLastModified(calendar.getTime());
					objectSummaries.add(s3ObjectSummary);
				}
				return objectListing;
			}

		}).given(amazonS3).listObjects(any(ListObjectsRequest.class));

		for (final S3Object s3Object : S3_OBJECTS) {
			willAnswer(new Answer<S3Object>() {

				@Override
				public S3Object answer(InvocationOnMock invocation) throws Throwable {
					return s3Object;
				}

			}).given(amazonS3).getObject(S3_BUCKET, s3Object.getKey());
		}


		new DirectFieldAccessor(s3Session).setPropertyValue("amazonS3", amazonS3);
		this.s3ChannelAdapter.start();
	}

	public abstract void test() throws Exception;

	@IntegrationTest({ "file.consumer.mode=ref", "s3.filenameRegex=.*\\\\.test$" })
	public static class AmazonS3FilesTransferredTests extends AmazonS3SourceMockTests {


		@Test
		@Override
		public void test() throws Exception {
			for (int i = 1; i <= 2; i++) {
				Message<?> received = this.messageCollector.forChannel(this.channels.output())
						.poll(10, TimeUnit.SECONDS);
				assertNotNull(received);
				assertThat(received, hasPayload(new File(this.config.getLocalDir(), i + ".test")));
			}

			assertEquals(2, this.config.getLocalDir().list().length);

			AWSCredentialsProvider awsCredentialsProvider =
					TestUtils.getPropertyValue(this.amazonS3, "awsCredentialsProvider", AWSCredentialsProvider.class);

			AWSCredentials credentials = awsCredentialsProvider.getCredentials();
			assertEquals(AWS_ACCESS_KEY, credentials.getAWSAccessKeyId());
			assertEquals(AWS_SECRET_KEY, credentials.getAWSSecretKey());

			assertEquals(Region.US_GovCloud, this.amazonS3.getRegion());
			assertEquals(new URI("https://s3-us-gov-west-1.amazonaws.com"),
					TestUtils.getPropertyValue(this.amazonS3, "endpoint"));
		}

	}

	@IntegrationTest({ "file.consumer.mode=lines", "s3.filenamePattern=otherFile", "file.consumer.with-markers=false" })
	public static class AmazonS3LinesTransferredTests extends AmazonS3SourceMockTests {


		@Test
		@Override
		public void test() throws Exception {
			BlockingQueue<Message<?>> messages = this.messageCollector.forChannel(this.channels.output());
			Message<?> received = messages.poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received, hasPayload("Other"));
			assertThat(received,
					hasHeader(FileHeaders.ORIGINAL_FILE, new File(this.config.getLocalDir(), "otherFile")));

			received = messages.poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received, hasPayload("Other2"));

			assertNull(messages.poll(10, TimeUnit.MILLISECONDS));

			assertEquals(1, this.config.getLocalDir().list().length);
		}

	}

	@SpringBootApplication
	public static class S3SourceApplication {

	}

}
