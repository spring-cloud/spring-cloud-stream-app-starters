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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeploymentProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.task.launcher.annotation.EnableTaskLauncher;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for the TaskLauncherSink.
 *
 * @author Glenn Renfro
 */
@EnableBinding(Sink.class)
@AutoConfigureBefore({CloudFoundryDeployerAutoConfiguration.class})
@EnableTaskLauncher
public class TaskLauncherCloudfoundrySinkConfiguration {

	/**
	 * Sets the prefix for the taskDeploymentProperties to "deployer"
	 * instead of the full "spring.cloud.deployer.cloudfoundry" prefix.  This
	 * also prevents a conflict with Spring Cloud Data Flow's usage of
	 * the deployer prefix.
	 *
	 * @return Instance of the CloudFoundryDeploymentProperties.
	 */
	@Bean(name = "taskDeploymentProperties")
	@ConfigurationProperties(prefix = "deployer")
	public CloudFoundryDeploymentProperties taskDeploymentProperties() {
		return new CloudFoundryDeploymentProperties();
	}

	/**
	 * Sets the prefix for the CloudFoundryConnectionProperties to "deployer"
	 * instead of the full "spring.cloud.deployer.cloudfoundry" prefix.  This
	 * also prevents a conflict with Spring Cloud Data Flow's usage of
	 * the deployer prefix.
	 *
	 * @return Instance of the CloudFoundryConnectionProperties.
	 */
	@Bean
	@ConfigurationProperties(prefix = "deployer")
	public CloudFoundryConnectionProperties cloudFoundryConnectionProperties() {
		return new CloudFoundryConnectionProperties();
	}
}
