/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.aws;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackStatus;

/**
 * @author Artem Bilan
 */
public class AwsIntegrationTestStackRule extends ExternalResource {

	private static final Log logger = LogFactory.getLog(AwsIntegrationTestStackRule.class);

	private AmazonCloudFormation cloudFormation;

	private String stackName;

	@Override
	protected void before() throws Throwable {
		try {
			String awsCredentialsDir = System.getProperty("aws.credentials.path");
			File awsCredentialsFile = new File(awsCredentialsDir, "aws.credentials.properties");
			Properties awsCredentials = new Properties();
			awsCredentials.load(new FileReader(awsCredentialsFile));
			String accessKey = awsCredentials.getProperty("cloud.aws.credentials.accessKey");
			String secretKey = awsCredentials.getProperty("cloud.aws.credentials.secretKey");
			this.cloudFormation = new AmazonCloudFormationClient(new BasicAWSCredentials(accessKey, secretKey));

			YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
			yamlPropertiesFactoryBean.setResources(new ClassPathResource("application.yml"));
			Properties applicationProperties = yamlPropertiesFactoryBean.getObject();

			this.stackName = applicationProperties.getProperty("cloud.aws.stack.name");

			after();

			ClassPathResource stackTemplate = new ClassPathResource("AwsIntegrationTestTemplate.json");
			String templateBody = FileCopyUtils.copyToString(new InputStreamReader(stackTemplate.getInputStream()));

			this.cloudFormation.createStack(
					new CreateStackRequest()
							.withTemplateBody(templateBody)
							.withOnFailure(OnFailure.DELETE)
							.withStackName(this.stackName));

			waitForCompletion();

			System.setProperty("cloud.aws.credentials.accessKey", accessKey);
			System.setProperty("cloud.aws.credentials.secretKey", secretKey);
		}
		catch (Exception e) {
			if (!(e instanceof AssumptionViolatedException)) {
				Assume.assumeTrue("Can't perform AWS integration test because of: " + e.getMessage(), false);
			}
			else {
				throw e;
			}
		}
	}

	@Override
	protected void after() {
		this.cloudFormation.deleteStack(new DeleteStackRequest()
				.withStackName(this.stackName));
		try {
			waitForCompletion();
		}
		catch (InterruptedException e) {
			// Ignore the InterruptedException
		}
		finally {
			System.clearProperty("cloud.aws.credentials.accessKey");
			System.clearProperty("cloud.aws.credentials.secretKey");
		}
	}

	// Wait for a stack to complete transitioning
	// End stack states are:
	//    CREATE_COMPLETE
	//    CREATE_FAILED
	//    DELETE_FAILED
	//    ROLLBACK_FAILED
	// OR the stack no longer exists
	private void waitForCompletion() throws InterruptedException {
		DescribeStacksRequest wait = new DescribeStacksRequest();
		wait.setStackName(this.stackName);
		Boolean completed = false;
		String stackStatus = "Unknown";
		String stackReason = "";

		while (!completed) {
			List<Stack> stacks = null;
			try {
				stacks = this.cloudFormation.describeStacks(wait).getStacks();
			}
			catch (Exception e) {
				logger.error("cloudFormation.describeStacks() exception", e);
			}
			if (CollectionUtils.isEmpty(stacks)) {
				completed = true;
				stackStatus = StackStatus.DELETE_COMPLETE.toString();
				stackReason = "Stack has been deleted";
			}
			else {
				for (Stack stack : stacks) {
					if (stack.getStackStatus().equals(StackStatus.CREATE_COMPLETE.toString()) ||
							stack.getStackStatus().equals(StackStatus.CREATE_FAILED.toString()) ||
							stack.getStackStatus().equals(StackStatus.ROLLBACK_FAILED.toString()) ||
							stack.getStackStatus().equals(StackStatus.DELETE_FAILED.toString())) {
						completed = true;
						stackStatus = stack.getStackStatus();
						stackReason = stack.getStackStatusReason();
					}
				}
			}

			// Not done yet so sleep for 2 seconds.
			if (!completed) {
				Thread.sleep(2000);
			}
			else {
				if (stackStatus.equals(StackStatus.CREATE_FAILED.toString())) {
					Assume.assumeTrue("The test AWS stack [" + this.stackName + "] cannot be created. Reason: " +
									stackReason, true);
				}
			}
		}
	}

}
