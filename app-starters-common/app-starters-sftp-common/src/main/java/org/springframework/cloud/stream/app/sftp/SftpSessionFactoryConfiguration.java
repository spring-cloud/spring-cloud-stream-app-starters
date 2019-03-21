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

package org.springframework.cloud.stream.app.sftp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * SFTP Session factory configuration.
 *
 * @author Gary Russell
 */
@Configuration
@EnableConfigurationProperties(SftpSessionFactoryProperties.class)
public class SftpSessionFactoryConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public SessionFactory<LsEntry> sftpSessionFactory(SftpSessionFactoryProperties properties) {
		DefaultSftpSessionFactory sftpSessionFactory = new DefaultSftpSessionFactory();
		sftpSessionFactory.setHost(properties.getHost());
		sftpSessionFactory.setPort(properties.getPort());
		sftpSessionFactory.setUser(properties.getUsername());
		sftpSessionFactory.setPassword(properties.getPassword());
		sftpSessionFactory.setAllowUnknownKeys(properties.isAllowUnknownKeys());
		if (properties.getKnownHostsExpression() != null) {
			sftpSessionFactory.setKnownHosts("#{" + properties.getKnownHostsExpression() + "}");
		}
		if (properties.getCacheSessions() != null) {
			CachingSessionFactory<LsEntry> csf = new CachingSessionFactory<>(sftpSessionFactory);
			return csf;
		}
		else {
			return sftpSessionFactory;
		}
	}

}
