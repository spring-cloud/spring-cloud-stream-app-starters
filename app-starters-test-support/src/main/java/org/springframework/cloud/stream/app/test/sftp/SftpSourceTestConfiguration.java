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

package org.springframework.cloud.stream.app.test.sftp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.mockito.Mockito;

import org.springframework.cloud.stream.app.test.BinderTestPropertiesInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Gary Russell
 *
 */
@Configuration
public class SftpSourceTestConfiguration {

	@Bean
	public static BinderTestPropertiesInitializer loadProps(ConfigurableApplicationContext context) {
		// minimal properties for the context to load
		Properties properties = new Properties();
		properties.put("username", "foo");
		return new BinderTestPropertiesInitializer(context, properties);
	}

	@Bean
	public SessionFactory<LsEntry> sftpSessionFactory() {
		@SuppressWarnings("unchecked")
		SessionFactory<LsEntry> ftpSessionFactory = Mockito.mock(SessionFactory.class);
		@SuppressWarnings("unchecked")
		Session<LsEntry> session = mock(Session.class);
		when(ftpSessionFactory.getSession()).thenReturn(session);
		return ftpSessionFactory;
	}

}

