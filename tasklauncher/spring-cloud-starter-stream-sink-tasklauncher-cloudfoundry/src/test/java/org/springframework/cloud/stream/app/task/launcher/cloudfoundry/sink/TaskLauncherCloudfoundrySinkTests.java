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

	@IntegrationTest({ "deployer.url = http://hello", "deployer.password = bar",
			"deployer.username = foo", "deployer.space = space",
			"deployer.org = org", "deployer.domain=baz",
			"deployer.services=mydb", "deployer.taskTimeout=123"})
	public static class PropertiesPopulatedTests extends TaskLauncherCloudfoundrySinkTests {

		@Test
		public void test() throws Exception {
			assertEquals("http://hello", cloudFoundryConnectionProperties.getUrl().toString());
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
