/*
 * Copyright (c) 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License") ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gemfire.source;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.app.test.gemfire.process.ProcessExecutor;
import org.springframework.cloud.stream.app.test.gemfire.process.ProcessWrapper;
import org.springframework.cloud.stream.app.test.gemfire.process.ServerProcess;
import org.springframework.cloud.stream.app.test.gemfire.support.FileSystemUtils;
import org.springframework.cloud.stream.app.test.gemfire.support.ThreadUtils;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.data.gemfire.client.Interest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;

/**
 * @author David Turanski
 **/
@RunWith(SpringJUnit4ClassRunner.class)

@SpringApplicationConfiguration(classes = {
		GemfireSourceConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class,
		TestSupportBinderAutoConfiguration.class })
@IntegrationTest({ "gemfire.region.regionName=Stocks"})
@EnableConfigurationProperties(GemfireSourceProperties.class)
public class GemfireSourceConfigurationTests {

	@Resource(name = "clientRegion")
	private Region region;

	@Autowired
	private Pool pool;

	@Autowired
	private List<Interest> interests;

	private static ProcessWrapper serverProcess;


	@BeforeClass
	public static void setup() throws IOException {
		System.out.println(System.getProperty("java.home"));
		String serverName = "GemFireTestServer";

		File serverWorkingDirectory = new File(FileSystemUtils.WORKING_DIRECTORY, serverName.toLowerCase());

		assertTrue(serverWorkingDirectory.isDirectory() || serverWorkingDirectory.mkdirs());

		List<String> arguments = new ArrayList<String>();

		arguments.add("-Dgemfire.name=" + serverName);
		arguments.add("gemfire-server.xml");

		serverProcess = ProcessExecutor.launch(serverWorkingDirectory, ServerProcess.class,
				arguments.toArray(new String[arguments.size()]));

		waitForServerStart(TimeUnit.SECONDS.toMillis(20));
	}

	private static void waitForServerStart(final long milliseconds) {
		ThreadUtils.timedWait(milliseconds, TimeUnit.MILLISECONDS.toMillis(500), new ThreadUtils.WaitCondition() {
			private File serverPidControlFile = new File(serverProcess.getWorkingDirectory(),
					ServerProcess.getServerProcessControlFilename());

			@Override
			public boolean waiting() {
				return !serverPidControlFile.isFile();
			}
		});
	}


	@Test
	@Ignore("No Subscription Servers available")
	public void testDefaultConfiguration() throws InterruptedException {
		assertThat("interests not present", interests.size() >= 1);
		assertThat("subscriptions should be enabled", pool.getSubscriptionEnabled());
	}


	@AfterClass
	public static void tearDown() {
		serverProcess.shutdown();

		if (Boolean.valueOf(System.getProperty("spring.gemfire.fork.clean", Boolean.TRUE.toString()))) {
			org.springframework.util.FileSystemUtils.deleteRecursively(serverProcess.getWorkingDirectory());
		}
	}

}
