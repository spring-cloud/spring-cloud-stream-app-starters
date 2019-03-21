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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.app.gpfdist.sink.support.SegmentRejectType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

public class GpfdistSinkPropertiesTests {

	@Test
	public void testErrorTable1() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--gpfdist.errorTable=myerror",
						"--gpfdist.segmentRejectLimit=1",
						"--gpfdist.segmentRejectType=ROWS" });

		GpfdistSinkProperties properties = context.getBean(GpfdistSinkProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.getErrorTable(), is("myerror"));
		assertThat(properties.getSegmentRejectLimit(), is("1"));
		assertThat(properties.getSegmentRejectType(), is(SegmentRejectType.ROWS));
		context.close();
	}

	@Test
	public void testErrorTable2() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--gpfdist.errorTable=myerror",
						"--gpfdist.segmentRejectLimit=1",
						"--gpfdist.segmentRejectType=percent" });

		GpfdistSinkProperties properties = context.getBean(GpfdistSinkProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.getErrorTable(), is("myerror"));
		assertThat(properties.getSegmentRejectLimit(), is("1"));
		assertThat(properties.getSegmentRejectType(), is(SegmentRejectType.PERCENT));
		context.close();
	}

	@Test
	public void testNullString() {
		SpringApplication app = new SpringApplication(TestConfiguration.class);
		app.setWebEnvironment(false);
		ConfigurableApplicationContext context = app
				.run(new String[] { "--gpfdist.nullString=mynullstring" });

		GpfdistSinkProperties properties = context.getBean(GpfdistSinkProperties.class);
		assertThat(properties, notNullValue());
		assertThat(properties.getNullString(), is("mynullstring"));
		context.close();
	}

	@Configuration
	@EnableConfigurationProperties({ GpfdistSinkProperties.class })
	protected static class TestConfiguration {
	}
}
