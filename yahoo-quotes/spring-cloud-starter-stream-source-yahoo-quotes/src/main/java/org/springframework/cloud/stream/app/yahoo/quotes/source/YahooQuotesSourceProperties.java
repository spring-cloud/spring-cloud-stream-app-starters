/*
 *  Copyright 2016 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.springframework.cloud.stream.app.yahoo.quotes.source;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Vinicius Carvalho
 */
@ConfigurationProperties("yahoo.quotes")
public class YahooQuotesSourceProperties {

	private String symbols;
	private String fields;
	private Integer batchSize = 400;
	private String cronExpression = "0 * 9-17 * * MON-FRI";
	private String zone = "EST";
	private Integer throttle;

	public Integer getThrottle() {
		return throttle;
	}

	public void setThrottle(Integer throttle) {
		this.throttle = throttle;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

}
