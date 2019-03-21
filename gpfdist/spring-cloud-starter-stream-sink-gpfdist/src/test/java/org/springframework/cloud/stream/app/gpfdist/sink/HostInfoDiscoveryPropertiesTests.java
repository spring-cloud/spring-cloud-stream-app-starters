/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.gpfdist.sink;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HostInfoDiscoveryPropertiesTests {

	@Test
	public void testAllSet() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--spring.net.hostdiscovery.pointToPoint=true",
						"--spring.net.hostdiscovery.loopback=true",
						"--spring.net.hostdiscovery.preferInterface=lxcbr",
						"--spring.net.hostdiscovery.matchIpv4=192.168.0.0/24",
						"--spring.net.hostdiscovery.matchInterface=eth0" });

		HostInfoDiscoveryProperties properties = context.getBean(HostInfoDiscoveryProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.isPointToPoint(), is(true));
		assertThat(properties.isLoopback(), is(true));
		assertThat(properties.getPreferInterface(), notNullValue());
		assertThat(properties.getPreferInterface().size(), is(1));
		assertThat(properties.getMatchIpv4(), is("192.168.0.0/24"));
		assertThat(properties.getMatchInterface(), is("eth0"));
		context.close();
	}

	@Test
	public void testPreferOne() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--spring.net.hostdiscovery.preferInterface=lxcbr" });

		HostInfoDiscoveryProperties properties = context.getBean(HostInfoDiscoveryProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.getPreferInterface(), notNullValue());
		assertThat(properties.getPreferInterface().size(), is(1));
		context.close();
	}

	@Test
	public void testPreferTwo() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--spring.net.hostdiscovery.preferInterface=lxcbr,foo" });

		HostInfoDiscoveryProperties properties = context.getBean(HostInfoDiscoveryProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.getPreferInterface(), notNullValue());
		assertThat(properties.getPreferInterface().size(), is(2));
		assertThat(properties.getPreferInterface(), containsInAnyOrder("lxcbr", "foo"));
		context.close();
	}

	@Configuration
	@EnableConfigurationProperties({ HostInfoDiscoveryProperties.class })
	protected static class TestConfiguration {
	}
}
