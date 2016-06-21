/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.gemfire.source;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * This represents the configuration properties for the Gemfire Source.
 *
 * @author David Turanski
 */
@ConfigurationProperties("gemfire")
public class GemfireSourceProperties {

	private static final String DEFAULT_EXPRESSION = "newValue";

	/**
	 * SpEL expression to extract fields from a cache event.
	 */
	private Expression expression = new SpelExpressionParser().parseExpression
			(DEFAULT_EXPRESSION);

	public Expression getCacheEventExpression() {
		return expression;
	}

	public void setCacheEventExpression(Expression cacheEventExpression) {
		this.expression = cacheEventExpression;
	}
}
