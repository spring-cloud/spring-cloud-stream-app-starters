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

package org.springframework.cloud.stream.app.jdbc.source;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultOne;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.scheduling.PollerMetadata;

/**
 * A module that reads data from an RDBMS using JDBC and creates a payload with the data.
 *
 * @author Thomas Risberg
 */
@EnableBinding(Source.class)
@Import(TriggerConfiguration.class)
@EnableConfigurationProperties({JdbcSourceProperties.class, TriggerPropertiesMaxMessagesDefaultOne.class})
public class JdbcSourceConfiguration {

	@Autowired
	@Qualifier("defaultPoller")
	private PollerMetadata poller;

	@Autowired
	private JdbcSourceProperties properties;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Bindings(JdbcSourceConfiguration.class)
	private Source source;

	@Bean
	public MessageSource<Object> jdbcMessageSource() {
		JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
				new JdbcPollingChannelAdapter(this.dataSource, this.properties.getQuery());
		jdbcPollingChannelAdapter.setMaxRowsPerPoll(this.properties.getMaxRowsPerPoll());
		jdbcPollingChannelAdapter.setUpdateSql(this.properties.getUpdate());
		return jdbcPollingChannelAdapter;
	}

	@Bean
	public IntegrationFlow pollingFlow() {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(jdbcMessageSource(),
				new Consumer<SourcePollingChannelAdapterSpec>() {

					@Override
					public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
						sourcePollingChannelAdapterSpec.poller(poller);
					}

				});
		if (this.properties.isSplit()) {
			flowBuilder.split();
		}
		flowBuilder.channel(this.source.output());
		return flowBuilder.get();
	}

}