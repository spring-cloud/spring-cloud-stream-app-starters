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

package org.springframework.cloud.stream.app.httpclient.processor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * Configuration properties for the Http Client Processor module.
 *
 * @author Waldemar Hummer
 * @author Mark Fisher
 */
@ConfigurationProperties("httpclient")
public class HttpclientProcessorProperties {

	private static final HttpMethod DEFAULT_HTTP_METHOD = HttpMethod.GET;

	private static final Class<?> DEFAULT_RESPONSE_TYPE = String.class;

	/**
	 * The URL to issue an http request to, as a static value.
	 */
	private String url;

	/**
	 * A SpEL expression against incoming message to determine the URL to use.
	 */
	private Expression urlExpression;

	/**
	 * The kind of http method to use.
	 */
	private HttpMethod httpMethod = DEFAULT_HTTP_METHOD;

	/**
	 * The (static) request body; if neither this nor bodyExpression is provided, the payload will be used.
	 */
	private Object body;

	/**
	 * A SpEL expression to derive the request body from the incoming message.
	 */
	private Expression bodyExpression;

	/**
	 * A SpEL expression used to derive the http headers map to use.
	 */
	private Expression headersExpression;

	/**
	 * The type used to interpret the response.
	 */
	private Class<?> expectedResponseType = DEFAULT_RESPONSE_TYPE;

	/**
	 * A SpEL expression used to compute the final result, applied against the whole http response.
	 */
	private Expression replyExpression = new SpelExpressionParser().parseExpression("body");

	public void setUrl(String url) {
		this.url = url;
	}

	public Expression getUrlExpression() {
		return urlExpression != null ? urlExpression
				: new LiteralExpression(this.url);
	}

	public void setUrlExpression(Expression urlExpression) {
		this.urlExpression = urlExpression;
	}

	@NotNull
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Expression getBodyExpression() {
		return bodyExpression;
	}

	public void setBodyExpression(Expression bodyExpression) {
		this.bodyExpression = bodyExpression;
	}

	public Expression getHeadersExpression() {
		return headersExpression;
	}

	public void setHeadersExpression(Expression headersExpression) {
		this.headersExpression = headersExpression;
	}

	@NotNull
	public Class<?> getExpectedResponseType() {
		return expectedResponseType;
	}

	public void setExpectedResponseType(Class<?> expectedResponseType) {
		this.expectedResponseType = expectedResponseType;
	}

	@NotNull
	public Expression getReplyExpression() {
		return replyExpression;
	}

	public void setReplyExpression(Expression replyExpression) {
		this.replyExpression = replyExpression;
	}

	@AssertTrue(message = "Exactly one of 'url' or 'urlExpression' is required")
	public boolean isExactlyOneUrl() {
		return url == null ^ urlExpression == null;
	}

	@AssertTrue(message = "At most one of 'body' or 'bodyExpression' is allowed")
	public boolean isAtMostOneBody() {
		return body == null || bodyExpression == null;
	}

}
