/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.app.jdbc.source;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds configuration properties for the Jdbc Source module.
 *
 * @author Thomas Risberg
 */
@ConfigurationProperties("jdbc")
public class JdbcSourceProperties {

	/**
	 * The query to use to select data.
	 */
	private String query;

	/**
	 * An SQL update statement to execute for marking polled messages as 'seen'.
	 */
	private String update;

	/**
	 * Whether to split the SQL result as individual messages.
	 */
	private boolean split = true;

	/**
	 * Max numbers of rows to process for each poll.
	 */
	private int maxRowsPerPoll = 0;

	@NotNull
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public boolean isSplit() {
		return split;
	}

	public void setSplit(boolean split) {
		this.split = split;
	}

	public int getMaxRowsPerPoll() {
		return maxRowsPerPoll;
	}

	public void setMaxRowsPerPoll(int maxRowsPerPoll) {
		this.maxRowsPerPoll = maxRowsPerPoll;
	}
}