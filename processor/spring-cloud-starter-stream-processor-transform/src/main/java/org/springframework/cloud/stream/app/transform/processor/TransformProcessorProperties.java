/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.transform.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Configuration properties for the Transform Processor module.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties("transformer")
public class TransformProcessorProperties {

	private static final Expression DEFAULT_EXPRESSION = new SpelExpressionParser().parseExpression("payload");

	private Expression expression = DEFAULT_EXPRESSION;

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

}
