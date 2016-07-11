/*
 * Copyright 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.cloud.stream.app.triggertask.source;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.cloud.task.launcher.TaskLaunchRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Triggertask source tests.
 * @author Glenn Renfro
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TriggertaskSourceTests.TriggerTaskSourceApplication.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public abstract class TriggertaskSourceTests {

	private static final String FIXED_DELAY = "fixedDelay=2";

	private static final String INITIAL_DELAY = "initialDelay=1";

	private static final String CRON_DELAY = "cron=0/2 * * * * *";

	private static final String URI_KEY = "triggertask.uri=";

	private static final String BASE_URI = "hello.world";

	private static final String CRON_URI = BASE_URI + "_CRON";

	private static final String []PARAMS = {"param1=test", "param2=another test", "param3=boo"};

	private static final String COMMAND_LINE_ARGS_KEY = "triggertask.commandLineArgs=";

	private static final String COMMAND_LINE_ARGS = "param1='test' param2='another test' param3=boo";

	private static final String ENVIRONMENT_PROPERTIES_KEY = "triggertask.environmentProperties=";

	private static final String ENVIRONMENT_PROPERTIES ="prop.1=foo, prop.2=bar,prop.3=baz";

	private static final String DEPLOYMENT_PROPERTIES_KEY = "triggertask.deploymentProperties=";

	private static final String DEPLOYMENT_PROPERTIES ="prop.1=aaa, prop.2=bbb,prop.3=ccc";

	private static final String PROP_PREFIX = "prop.";

	private static final Map<String, String> environmentPropertyMap = new HashMap<>();
	static
	{
		environmentPropertyMap.put(PROP_PREFIX + "1", "foo");
		environmentPropertyMap.put(PROP_PREFIX + "2", "bar");
		environmentPropertyMap.put(PROP_PREFIX + "3", "baz");

	}

	private static final Map<String, String> deploymentPropertyMap = new HashMap<>();
	static
	{
		deploymentPropertyMap.put(PROP_PREFIX + "1", "aaa");
		deploymentPropertyMap.put(PROP_PREFIX + "2", "bbb");
		deploymentPropertyMap.put(PROP_PREFIX + "3", "ccc");

	}

	@Autowired
	@Bindings(TriggertaskSourceConfiguration.class)
	protected Source triggerSource;

	@Autowired
	protected MessageCollector messageCollector;

	@IntegrationTest({FIXED_DELAY, INITIAL_DELAY, URI_KEY + BASE_URI,
			COMMAND_LINE_ARGS_KEY + COMMAND_LINE_ARGS,
			ENVIRONMENT_PROPERTIES_KEY + ENVIRONMENT_PROPERTIES,
			DEPLOYMENT_PROPERTIES_KEY + DEPLOYMENT_PROPERTIES})
	public static class FixedDelayTest extends TriggertaskSourceTests {

		@Test
		public void fixedDelayTest() throws InterruptedException {
			TaskLaunchRequest tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
			assertEquals(BASE_URI, tlr.getUri());
			checkConfigurationSize(3, 3, 3, tlr);

			for (int i = 0; i < 3; i++) {
				assertEquals(String.format("the expected environment property for %s was %s but expected %s",
						PROP_PREFIX + i, tlr.getEnvironmentProperties().get(PROP_PREFIX + i), environmentPropertyMap.get(PROP_PREFIX + i )),
						environmentPropertyMap.get(PROP_PREFIX + i ), tlr.getEnvironmentProperties().get(PROP_PREFIX + i));

				assertEquals(String.format("the expected deployment property for %s was %s but expected %s",
						PROP_PREFIX + i, tlr.getDeploymentProperties().get(PROP_PREFIX + i), deploymentPropertyMap.get(PROP_PREFIX + i )),
						deploymentPropertyMap.get(PROP_PREFIX + i ), tlr.getDeploymentProperties().get(PROP_PREFIX + i));

				assertEquals(String.format("the expected commandLineArg was %s but expected param%s", i,
						tlr.getCommandlineArguments().get(i), PARAMS[i]),
						PARAMS[i], tlr.getCommandlineArguments().get(i));
			}
		}
	}

	@IntegrationTest({FIXED_DELAY, INITIAL_DELAY, URI_KEY + BASE_URI})
	public static class FixedDelayTestNoArgumentsTest extends TriggertaskSourceTests {

		@Test
		public void fixedDelayNoCommantLineArgumentsTest() throws InterruptedException {
			TaskLaunchRequest tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
			assertEquals(BASE_URI, tlr.getUri());
			checkConfigurationSize(0, 0, 0, tlr);
		}
	}

	@IntegrationTest({FIXED_DELAY, INITIAL_DELAY})
	public static class MissingURITest extends TriggertaskSourceTests {

		@Test(expected = NullPointerException.class)
		public void MissingURITest() throws InterruptedException {
			TaskLaunchRequest tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
		}
	}

	@IntegrationTest({FIXED_DELAY, INITIAL_DELAY, URI_KEY + BASE_URI})
	public static class FixedDelayEmptyPayloadTest extends TriggertaskSourceTests {

		@Test
		public void fixedDelayTest() throws InterruptedException {
			TaskLaunchRequest tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
			assertEquals(BASE_URI, tlr.getUri());
			checkConfigurationSize(0, 0, 0, tlr);
		}
	}

	@IntegrationTest({"trigger.cron = 0/2 * * * * *", CRON_DELAY, URI_KEY + CRON_URI})
	public static class CronTriggerTest extends TriggertaskSourceTests {

		@Test
		public void cronTriggerTest() throws InterruptedException {
			TaskLaunchRequest tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
			assertEquals(CRON_URI, tlr.getUri());
			tlr = (TaskLaunchRequest) messageCollector.forChannel(triggerSource.output()).poll(2100, TimeUnit.MILLISECONDS).getPayload();
			assertEquals(CRON_URI, tlr.getUri());
			checkConfigurationSize(0, 0, 0, tlr);
		}
	}

	@SpringBootApplication
	public static class TriggerTaskSourceApplication {

	}

	private static void checkConfigurationSize(int commandLineArgSize, int environmentPropertySize, int deploymentPropertySize, TaskLaunchRequest tlr) {
		assertEquals(String.format("expected %d commandLineArgs", commandLineArgSize), commandLineArgSize, tlr.getCommandlineArguments().size());
		assertEquals(String.format("expected %d environmentProperties", environmentPropertySize), environmentPropertySize, tlr.getEnvironmentProperties().size());
		assertEquals(String.format("expected %d deploymentProperties", deploymentPropertySize), deploymentPropertySize, tlr.getDeploymentProperties().size());
	}
}
