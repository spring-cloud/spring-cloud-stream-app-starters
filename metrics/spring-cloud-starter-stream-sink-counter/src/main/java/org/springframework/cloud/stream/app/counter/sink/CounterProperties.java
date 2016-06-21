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
package org.springframework.cloud.stream.app.counter.sink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;

import javax.validation.constraints.AssertTrue;

/**
 * Common configuration properties for the Spring Cloud Stream metric modules.
 *
 * @author Eric Bottard
 * @author Ilayaperumal Gopinathan
 * @author Gary Russell
 */
@ConfigurationProperties("counter")
public class CounterProperties {

	/**
	 * The default name of the counter
	 */
	@Value("${spring.application.name:counts}")
	private String defaultName;

	/**
	 * The name of the counter to increment.
	 */
	private String name;

	/**
	 * A SpEL expression (against the incoming Message) to derive the name of the counter to increment.
	 */
	private Expression nameExpression;

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

}
