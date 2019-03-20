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

package org.springframework.cloud.stream.app.task.launcher.cloudfoundry.sink;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeploymentProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Properties for the Task Launcher Cloud Foundry Sink.
 *
 * @author Glenn Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TaskLauncherCloudfoundrySinkTests.TaskLauncherSinkApplication.class)
@DirtiesContext
@IntegrationTest()
public abstract class TaskLauncherCloudfoundrySinkTests {

	@Autowired
	protected CloudFoundryConnectionProperties cloudFoundryConnectionProperties;

	@Autowired
	protected CloudFoundryDeploymentProperties taskDeploymentProperties;

	@IntegrationTest({ "spring.cloud.deployer.cloudfoundry.url = https://hello", "spring.cloud.deployer.cloudfoundry.password = bar",
			"spring.cloud.deployer.cloudfoundry.username = foo", "spring.cloud.deployer.cloudfoundry.space = space",
			"spring.cloud.deployer.cloudfoundry.org = org", "spring.cloud.deployer.cloudfoundry.domain=baz",
			"spring.cloud.deployer.cloudfoundry.services=mydb", "spring.cloud.deployer.cloudfoundry.taskTimeout=123"})
	public static class PropertiesPopulatedTests extends TaskLauncherCloudfoundrySinkTests {

		@Test
		public void test() throws Exception {
			assertEquals("https://hello", cloudFoundryConnectionProperties.getUrl().toString());
			assertEquals("bar", cloudFoundryConnectionProperties.getPassword());
			assertEquals("foo", cloudFoundryConnectionProperties.getUsername());
			assertEquals("org", cloudFoundryConnectionProperties.getOrg());
			assertEquals("space", cloudFoundryConnectionProperties.getSpace());

			assertEquals("baz", taskDeploymentProperties.getDomain());
			assertEquals(123, taskDeploymentProperties.getTaskTimeout());
			assertTrue(taskDeploymentProperties.getServices().contains("mydb"));
		}
	}

	@SpringBootApplication
	public static class TaskLauncherSinkApplication {

	}
}
