/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.cassandra;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cassandra.config.CompressionType;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfiguration extends AbstractCassandraConfiguration {

	@Autowired
	CassandraProperties cassandraProperties;

	@Override
	protected String getContactPoints() {
		return cassandraProperties.getContactPoints();
	}

	@Override
	protected int getPort() {
		return cassandraProperties.getPort();
	}

	@Override
	protected String getKeyspaceName() {
		return cassandraProperties.getKeyspace();
	}

	@Override
	protected AuthProvider getAuthProvider() {
		if (StringUtils.hasText(this.cassandraProperties.getUsername())) {
			return new PlainTextAuthProvider(this.cassandraProperties.getUsername(),
					this.cassandraProperties.getPassword());
		}
		else {
			return null;
		}
	}

	@Override
	public SchemaAction getSchemaAction() {
		return this.cassandraProperties.getSchemaAction();
	}

	@Override
	public String[] getEntityBasePackages() {
		return this.cassandraProperties.getEntityBasePackages();
	}

	@Override
	protected CompressionType getCompressionType() {
		return this.cassandraProperties.getCompressionType();
	}

	@Override
	protected boolean getMetricsEnabled() {
		return this.cassandraProperties.isMetricsEnabled();
	}

	@Override
	protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
		if (this.cassandraProperties.isCreateKeyspace()) {
			return Collections.singletonList(CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
					.withSimpleReplication()
					.ifNotExists());
		}
		else {
			return super.getKeyspaceCreations();
		}
	}

	/**
	 * Inner class to execute init scripts on the provided {@code keyspace}.
	 * It is here to bypass circular dependency with {@link Session} injection and
	 * its {@code @amp;Bean} in the {@link AbstractCassandraConfiguration}.
	 * From other side we can't use {@link #getStartupScripts()} because they are
	 * shared between Cluster and Session initializations.
	 */
	@Configuration
	protected static class CassandraKeyspaceInitializerConfiguration {

		@Autowired
		CassandraProperties cassandraProperties;

		@Autowired
		Session session;

		@PostConstruct
		public void init() throws IOException {
			if (this.cassandraProperties.getInitScript() != null) {
				String scripts = new Scanner(this.cassandraProperties.getInitScript().getInputStream(),
						"UTF-8").useDelimiter("\\A").next();

				CqlTemplate template = new CqlTemplate(this.session);

				for (String script : StringUtils.delimitedListToStringArray(scripts, ";", "\r\n\f")) {
					if (StringUtils.hasText(script)) { // an empty String after the last ';'
						template.execute(script + ";");
					}
				}
			}
		}

	}

}
