/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.cassandra.sink;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.cassandra.CassandraConfiguration;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.AbstractMessageProducingHandler;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.integration.support.json.JsonObjectMapper;
import org.springframework.integration.transformer.AbstractPayloadTransformer;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 * @author Ashu Gairola
 */
@EnableBinding(Sink.class)
@Import(CassandraConfiguration.class)
@EnableConfigurationProperties(CassandraSinkProperties.class)
@PropertySource("cassandra-application.properties")
public class CassandraSinkConfiguration {

	@Autowired
	private CassandraSinkProperties cassandraSinkProperties;

	@Autowired
	private CassandraOperations template;

	@Bean
	public MessageChannel toSink() {
		return new DirectChannel();
	}

	@Bean
	@Primary
	@ServiceActivator(inputChannel = Sink.INPUT)
	public MessageHandler bridgeMessageHandler() {
		AbstractMessageProducingHandler messageHandler;
		if (StringUtils.hasText(this.cassandraSinkProperties.getIngestQuery())) {
			messageHandler = new MessageTransformingHandler(
					new PayloadToMatrixTransformer(this.cassandraSinkProperties.getIngestQuery()));
		}
		else {
			messageHandler = new BridgeHandler();
		}
		messageHandler.setOutputChannel(toSink());
		return messageHandler;
	}

	@Bean
	@ServiceActivator(inputChannel = "toSink")
	public MessageHandler cassandraSinkMessageHandler() {
		CassandraMessageHandler<?> cassandraMessageHandler =
				this.cassandraSinkProperties.getQueryType() != null
						? new CassandraMessageHandler<>(this.template, this.cassandraSinkProperties.getQueryType())
						: new CassandraMessageHandler<>(this.template);
		cassandraMessageHandler.setProducesReply(false);
		if (this.cassandraSinkProperties.getConsistencyLevel() != null
				|| this.cassandraSinkProperties.getRetryPolicy() != null
				|| this.cassandraSinkProperties.getTtl() > 0) {
			cassandraMessageHandler.setWriteOptions(
					new WriteOptions(this.cassandraSinkProperties.getConsistencyLevel(),
							this.cassandraSinkProperties.getRetryPolicy(), this.cassandraSinkProperties.getTtl()));
		}
		if (StringUtils.hasText(this.cassandraSinkProperties.getIngestQuery())) {
			cassandraMessageHandler.setIngestQuery(this.cassandraSinkProperties.getIngestQuery());
		}
		else if (this.cassandraSinkProperties.getStatementExpression() != null) {
			cassandraMessageHandler.setStatementExpression(this.cassandraSinkProperties.getStatementExpression());
		}
		return cassandraMessageHandler;
	}

	private static boolean isUuid(String uuid) {
		if (uuid.length() == 36) {
			String[] parts = uuid.split("-");
			if (parts.length == 5) {
				if ((parts[0].length() == 8) && (parts[1].length() == 4) &&
						(parts[2].length() == 4) && (parts[3].length() == 4) &&
						(parts[4].length() == 12)) {
					return true;
				}
			}
		}
		return false;
	}


	private static class PayloadToMatrixTransformer extends AbstractPayloadTransformer<Object, List<List<Object>>> {

		private static final Pattern PATTERN = Pattern.compile(".+\\((.+)\\).+(?:values\\s*\\((.+)\\))");

		private final ObjectMapper objectMapper = new ObjectMapper();

		private final JsonObjectMapper<?, ?> jsonObjectMapper = new Jackson2JsonObjectMapper(this.objectMapper);

		private final List<String> columns = new LinkedList<>();

		private final ISO8601StdDateFormat dateFormat = new ISO8601StdDateFormat();

		public PayloadToMatrixTransformer(String query) {
			Matcher matcher = PATTERN.matcher(query);
			if (matcher.matches()) {
				String[] columns = StringUtils.delimitedListToStringArray(matcher.group(1), ",", " ");
				String[] params = StringUtils.delimitedListToStringArray(matcher.group(2), ",", " ");
				for (int i = 0; i < columns.length; i++) {
					String param = params[i];
					if (param.equals("?")) {
						this.columns.add(columns[i]);
					}
					else if (param.startsWith(":")) {
						this.columns.add(param.substring(1));
					}
				}
			}
			else {
				throw new IllegalArgumentException("Invalid CQL insert query syntax: " + query);
			}
			this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		}

		@Override
		@SuppressWarnings("unchecked")
		protected List<List<Object>> transformPayload(Object payload) throws Exception {
			if (payload instanceof List) {
				return (List<List<Object>>) payload;
			}
			else {
				List<Map<String, Object>> model = this.jsonObjectMapper.fromJson(payload, List.class);
				List<List<Object>> data = new ArrayList<>(model.size());
				for (Map<String, Object> entity : model) {
					List<Object> row = new ArrayList<>(this.columns.size());
					for (String column : this.columns) {
						Object value = entity.get(column);
						if (value instanceof String) {
							String string = (String) value;
							if (this.dateFormat.looksLikeISO8601(string)) {
								synchronized (this.dateFormat) {
									value = this.dateFormat.parse(string);
								}
							}
							if (isUuid(string)) {
								value = UUID.fromString(string);
							}
						}
						row.add(value);
					}
					data.add(row);
				}
				return data;
			}
		}

	}

	@SuppressWarnings("serial")
	/*
	 * We need this to provide visibility to the protected method.
	 */
	private static class ISO8601StdDateFormat extends StdDateFormat {

		@Override
		protected boolean looksLikeISO8601(String dateStr) {
			return super.looksLikeISO8601(dateStr);
		}

	}

}
