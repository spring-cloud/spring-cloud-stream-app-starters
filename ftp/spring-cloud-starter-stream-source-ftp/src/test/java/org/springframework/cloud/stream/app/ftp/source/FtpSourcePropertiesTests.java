/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.ftp.source;

import java.io.File;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cloud.stream.app.ftp.source.FtpSourceProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author David Turanski
 */
public class FtpSourcePropertiesTests {

	@Test
	public void localDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.localDir:local");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getLocalDir(), equalTo(new File("local")));
	}

	@Test
	public void remoteDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.remoteDir:/remote");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getRemoteDir(), equalTo("/remote"));
	}

	@Test
	public void deleteRemoteFilesCanBeEnabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.deleteRemoteFiles:true");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(properties.isDeleteRemoteFiles());
	}

	@Test
	public void autoCreateLocalDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.autoCreateLocalDir:false");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(!properties.isAutoCreateLocalDir());
	}

	@Test
	public void tmpFileSuffixCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.tmpFileSuffix:.foo");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getTmpFileSuffix(), equalTo(".foo"));
	}

	@Test
	public void filenamePatternCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.filenamePattern:*.foo");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getFilenamePattern(), equalTo("*.foo"));
	}

	@Test
	public void remoteFileSeparatorCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.remoteFileSeparator:\\");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertThat(properties.getRemoteFileSeparator(), equalTo("\\"));
	}


	@Test
	public void preserveTimestampDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "ftp.preserveTimestamp:false");
		context.register(Conf.class);
		context.refresh();
		FtpSourceProperties properties = context.getBean(FtpSourceProperties.class);
		assertTrue(!properties.isPreserveTimestamp());
	}

	@Configuration
	@EnableConfigurationProperties(FtpSourceProperties.class)
	static class Conf {

	}
}
