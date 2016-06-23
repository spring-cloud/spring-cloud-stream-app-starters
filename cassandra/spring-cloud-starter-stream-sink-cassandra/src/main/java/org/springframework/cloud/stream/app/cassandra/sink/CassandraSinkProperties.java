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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.expression.Expression;
import org.springframework.integration.cassandra.outbound.CassandraMessageHandler;

/**
 * @author Artem Bilan
 * @author Thomas Risberg
 */
@ConfigurationProperties("cassandra")
public class CassandraSinkProperties {

	/**
	 * The consistencyLevel option of WriteOptions.
	 */
	private ConsistencyLevel consistencyLevel;

	/**
	 * The retryPolicy option of WriteOptions.
	 */
	private RetryPolicy retryPolicy;

	/**
	 * The time-to-live option of WriteOptions.
	 */
	private int ttl;

	/**
	 * The queryType for Cassandra Sink.
	 */
	private CassandraMessageHandler.Type queryType;

	/**
	 * The ingest Cassandra query.
	 */
	private String ingestQuery;

	/**
	 * The expression in Cassandra query DSL style.
	 */
	private Expression statementExpression;

	public ConsistencyLevel getConsistencyLevel() {
		return consistencyLevel;
	}

	public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
		this.consistencyLevel = consistencyLevel;
	}

	public RetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public CassandraMessageHandler.Type getQueryType() {
		return queryType;
	}

	public void setQueryType(CassandraMessageHandler.Type queryType) {
		this.queryType = queryType;
	}

	public String getIngestQuery() {
		return ingestQuery;
	}

	public void setIngestQuery(String ingestQuery) {
		this.ingestQuery = ingestQuery;
	}

	public Expression getStatementExpression() {
		return statementExpression;
	}

	public void setStatementExpression(Expression statementExpression) {
		this.statementExpression = statementExpression;
	}

}
