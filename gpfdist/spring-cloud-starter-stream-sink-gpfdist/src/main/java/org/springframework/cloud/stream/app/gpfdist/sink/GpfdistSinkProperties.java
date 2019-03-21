/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gpfdist.sink;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.gpfdist.sink.support.SegmentRejectType;
import org.springframework.core.io.Resource;

/**
 * Config options for gpfdist sink.
 *
 * @author Janne Valkealahti
 * @author Sabby Anandan
 */
@ConfigurationProperties("gpfdist")
public class GpfdistSinkProperties {

	/**
	 * Port of gpfdist server. Default port `0` indicates that a random port is chosen. (Integer, default: 0)
	 */
	private int gpfdistPort = 0;

	/**
	 * Flush item count (int, default: 100)
	 */
	private int flushCount = 100;

	/**
	 * Flush item time (int, default: 2)
	 */
	private int flushTime = 2;

	/**
	 * Timeout in seconds for segment inactivity. (Integer, default: 4)
	 */
	private int batchTimeout = 4;

	/**
	 * Number of windowed batch each segment takest (int, default: 100)
	 */
	private int batchCount = 100;

	/**
	 * Time in seconds for each load operation to sleep in between operations (int, default: 10)
	 */
	private int batchPeriod = 10;

	/**
	 * Database name (String, default: gpadmin)
	 */
	private String dbName = "gpadmin";

	/**
	 * Database user (String, default: gpadmin)
	 */
	private String dbUser = "gpadmin";

	/**
	 * Database password (String, default: gpadmin)
	 */
	private String dbPassword = "gpadmin";

	/**
	 * Database host (String, default: localhost)
	 */
	private String dbHost = "localhost";

	/**
	 * Database port (int, default: 5432)
	 */
	private int dbPort = 5432;

	/**
	 * Path to yaml control file (String, no default)
	 */
	private Resource controlFile;

	/**
	 * Data line delimiter (String, default: newline character)
	 */
	private String delimiter = "\n";

	/**
	 * Data record column delimiter. *(Character, default: no default)
	 */
	private Character columnDelimiter;

	/**
	 * Mode, either insert or update (String, no default)
	 */
	private String mode;

	/**
	 * Match columns with update (String, no default)
	 */
	private String matchColumns;

	/**
	 * Update columns with update (String, no default)
	 */
	private String updateColumns;

	/**
	 * Target database table (String, no default)
	 */
	private String table;

	/**
	 * Enable transfer rate interval (int, default: 0)
	 */
	private int rateInterval = 0;

	/**
	 * Sql to run before load (String, no default)
	 */
	private String sqlBefore;

	/**
	 * Sql to run after load (String, no default)
	 */
	private String sqlAfter;

	/**
	 * Tablename to log errors. (String, default: ``)
	 */
	private String errorTable;

	/**
	 * Error reject limit. (String, default: ``)
	 */
	private String segmentRejectLimit;

	/**
	 * Error reject type, either `rows` or `percent`. (String, default: ``)
	 */
	private SegmentRejectType segmentRejectType;

	/**
	 * Null string definition. (String, default: ``)
	 */
	private String nullString;

	public int getGpfdistPort() {
		return gpfdistPort;
	}

	public void setGpfdistPort(int gpfdistPort) {
		this.gpfdistPort = gpfdistPort;
	}

	public int getFlushCount() {
		return flushCount;
	}

	public void setFlushCount(int flushCount) {
		this.flushCount = flushCount;
	}

	public int getFlushTime() {
		return flushTime;
	}

	public void setFlushTime(int flushTime) {
		this.flushTime = flushTime;
	}

	public int getBatchTimeout() {
		return batchTimeout;
	}

	public void setBatchTimeout(int batchTimeout) {
		this.batchTimeout = batchTimeout;
	}

	public int getBatchPeriod() {
		return batchPeriod;
	}

	public void setBatchPeriod(int batchPeriod) {
		this.batchPeriod = batchPeriod;
	}

	public int getBatchCount() {
		return batchCount;
	}

	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public int getDbPort() {
		return dbPort;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public Resource getControlFile() {
		return controlFile;
	}

	public void setControlFile(Resource controlFile) {
		this.controlFile = controlFile;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public Character getColumnDelimiter() {
		return columnDelimiter;
	}

	public void setColumnDelimiter(Character columnDelimiter) {
		this.columnDelimiter = columnDelimiter;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getUpdateColumns() {
		return updateColumns;
	}

	public void setUpdateColumns(String updateColumns) {
		this.updateColumns = updateColumns;
	}

	public String getMatchColumns() {
		return matchColumns;
	}

	public void setMatchColumns(String matchColumns) {
		this.matchColumns = matchColumns;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public int getRateInterval() {
		return rateInterval;
	}

	public void setRateInterval(int rateInterval) {
		this.rateInterval = rateInterval;
	}

	public String getSqlBefore() {
		return sqlBefore;
	}

	public void setSqlBefore(String sqlBefore) {
		this.sqlBefore = sqlBefore;
	}

	public String getSqlAfter() {
		return sqlAfter;
	}

	public void setSqlAfter(String sqlAfter) {
		this.sqlAfter = sqlAfter;
	}

	public String getErrorTable() {
		return errorTable;
	}

	public void setErrorTable(String errorTable) {
		this.errorTable = errorTable;
	}

	public String getSegmentRejectLimit() {
		return segmentRejectLimit;
	}

	public void setSegmentRejectLimit(String segmentRejectLimit) {
		this.segmentRejectLimit = segmentRejectLimit;
	}

	public SegmentRejectType getSegmentRejectType() {
		return segmentRejectType;
	}

	public void setSegmentRejectType(SegmentRejectType segmentRejectType) {
		this.segmentRejectType = segmentRejectType;
	}

	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
}
