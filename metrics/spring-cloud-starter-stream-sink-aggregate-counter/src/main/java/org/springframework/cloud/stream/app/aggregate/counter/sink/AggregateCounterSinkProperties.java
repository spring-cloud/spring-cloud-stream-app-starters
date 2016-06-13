/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.aggregate.counter.sink;

import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.time.DateFormat;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.expression.ValueExpression;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * @author Ilayaperumal Gopinathan
 */
@ConfigurationProperties("aggregate-counter")
public class AggregateCounterSinkProperties {

	/**
	 * The default name of the aggregate counter
	 */
	@Value("${spring.application.name:aggregate-counter}")
	private String defaultName;

	/**
	 * The name of the aggregate counter.
	 */
	private String name;

	/**
	 * A SpEL expression (against the incoming Message) to derive the name of the aggregate counter.
	 */
	private Expression nameExpression;

	/**
	 * A SpEL expression (against the incoming Message) to derive the timestamp value.
	 */
	private Expression timeField;

	/**
	 * Increment value for each bucket as a SpEL against the message
	 */
	private Expression incrementExpression = new ValueExpression<>(1L);

	private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public String getName() {
		if (name == null && nameExpression == null) {
			return defaultName;
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Expression getNameExpression() {
		return nameExpression;
	}

	public Expression getComputedNameExpression() {
		return (nameExpression != null ? nameExpression : new LiteralExpression(getName()));
	}

	public void setNameExpression(Expression nameExpression) {
		this.nameExpression = nameExpression;
	}

	@AssertTrue(message = "exactly one of 'name' and 'nameExpression' must be set")
	public boolean isExclusiveOptions() {
		return getName() != null ^ getNameExpression() != null;
	}

	public Expression getTimeField() {
		return timeField;
	}

	public void setTimeField(Expression timeField) {
		this.timeField = timeField;
	}

	public Expression getIncrementExpression() {
		return this.incrementExpression;
	}

	public void setIncrementExpression(Expression incrementExpression) {
		this.incrementExpression = incrementExpression;
	}

	@NotBlank
	@DateFormat
	public String getDateFormat() {
		return dateFormat;
	}

	@NotNull
	public DateTimeFormatter getDateFormatter() {
		return DateTimeFormat.forPattern(this.getDateFormat());
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

}