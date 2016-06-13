/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.sftp.source;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author David Turanski
 * @author Gary Russell
 */
public class SftpSourcePropertiesTests {

	@Test
	public void localDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.localDir:local");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getLocalDir(), equalTo(new File("local")));
	}

	@Test
	public void remoteDirCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.remoteDir:/remote");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getRemoteDir(), equalTo("/remote"));
	}

	@Test
	public void deleteRemoteFilesCanBeEnabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.deleteRemoteFiles:true");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertTrue(properties.isDeleteRemoteFiles());
	}

	@Test
	public void autoCreateLocalDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.autoCreateLocalDir:false");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertTrue(!properties.isAutoCreateLocalDir());
	}

	@Test
	public void tmpFileSuffixCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.tmpFileSuffix:.foo");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getTmpFileSuffix(), equalTo(".foo"));
	}

	@Test
	public void filenamePatternCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.filenamePattern:*.foo");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getFilenamePattern(), equalTo("*.foo"));
	}

	@Test
	public void filenameRegexCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.filenameRegex:.*\\.foo");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getFilenameRegex().toString(), equalTo(".*\\.foo"));
	}

	@Test
	public void remoteFileSeparatorCanBeCustomized() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.remoteFileSeparator:\\");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertThat(properties.getRemoteFileSeparator(), equalTo("\\"));
	}


	@Test
	public void preserveTimestampDirCanBeDisabled() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "sftp.preserveTimestamp:false");
		context.register(Conf.class);
		context.refresh();
		SftpSourceProperties properties = context.getBean(SftpSourceProperties.class);
		assertTrue(!properties.isPreserveTimestamp());
	}

	@Configuration
	@EnableConfigurationProperties(SftpSourceProperties.class)
	static class Conf {

	}
}
