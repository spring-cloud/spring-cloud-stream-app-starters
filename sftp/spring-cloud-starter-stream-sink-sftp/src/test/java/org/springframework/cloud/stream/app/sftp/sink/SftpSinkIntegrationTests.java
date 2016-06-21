/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.sftp.sink;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.app.test.PropertiesInitializer;
import org.springframework.cloud.stream.app.test.sftp.SftpTestSupport;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author David Turanski
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SftpSinkIntegrationTests.SftpSinkApplication.class,
								initializers = PropertiesInitializer.class)
@DirtiesContext
public class SftpSinkIntegrationTests extends SftpTestSupport {

	@BeforeClass
	public static void configureSink() throws Throwable {

		Properties properties = new Properties();
		properties.put("sftp.remoteDir", "sftpTarget");
		properties.put("sftp.factory.username", "foo");
		properties.put("sftp.factory.password", "foo");

		properties.put("sftp.factory.port", port);
		properties.put("sftp.mode", "FAIL");
		properties.put("sftp.filenameExpression", "payload.name.toUpperCase()");
		properties.put("sftp.factory.allowUnknownKeys", "true");
		PropertiesInitializer.PROPERTIES = properties;
	}

	@Autowired
	Sink sftpSink;

	@Test
	public void sendFiles() {
		for (int i = 1; i <= 2; i++) {
			String pathname = "/localSource" + i + ".txt";
			String upperPathname = pathname.toUpperCase();
			new File(getTargetRemoteDirectory() + upperPathname).delete();
			assertFalse(new File(getTargetRemoteDirectory() + upperPathname).exists());
			this.sftpSink.input().send(new GenericMessage<>(new File(getSourceLocalDirectory() + pathname)));
			File expected = new File(getTargetRemoteDirectory() + upperPathname);
			assertTrue(expected.getAbsolutePath() + " does not exist", expected.exists());
			// verify the upcase on a case-insensitive file system
			File[] files = getTargetRemoteDirectory().listFiles();
			for (File file : files) {
				assertThat(file.getName(), startsWith("LOCALSOURCE"));
			}
		}
	}

	@Test
	public void serverRefreshed() { // noop test to test the dirs are refreshed properly
		String pathname = "/LOCALSOURCE1.TXT";
		assertTrue(getTargetRemoteDirectory().exists());
		assertFalse(new File(getTargetRemoteDirectory() + pathname).exists());
	}

	@SpringBootApplication
	public static class SftpSinkApplication {

	}

}
