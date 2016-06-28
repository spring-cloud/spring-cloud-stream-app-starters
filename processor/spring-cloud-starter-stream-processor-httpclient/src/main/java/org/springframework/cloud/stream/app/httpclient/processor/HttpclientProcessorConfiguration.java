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

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.client.RestTemplate;

/**
 * A processor app that makes requests to an HTTP resource and emits the
 * response body as a message payload. This processor can be combined, e.g.,
 * with a time source module to periodically poll results from a HTTP resource.

 * @author Waldemar Hummer
 * @author Mark Fisher
 * @author Gary Russell
 */
@Configuration
@EnableBinding(Processor.class)
@EnableConfigurationProperties(HttpclientProcessorProperties.class)
public class HttpclientProcessorConfiguration {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public HttpClientProcessor httpClient() {
		return new HttpClientProcessor();
	}

	@MessageEndpoint
	public static class HttpClientProcessor {

		private static final Log LOG = LogFactory.getLog(HttpClientProcessor.class);

		@Autowired
		private HttpclientProcessorProperties properties;

		@Autowired
		private RestTemplate restTemplate;

		@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
		public Object process(Message<?> message) {
			try {
				/* construct headers */
				HttpHeaders headers = new HttpHeaders();
				if (properties.getHeadersExpression() != null) {
					Map<?, ?> headersMap = properties.getHeadersExpression().getValue(message, Map.class);
					for (Entry<?, ?> header : headersMap.entrySet()) {
						if (header.getKey() != null && header.getValue() != null) {
							headers.add(header.getKey().toString(),
									header.getValue().toString());
						}
					}
				}

				Class<?> responseType = properties.getExpectedResponseType();
				HttpMethod method = properties.getHttpMethod();
				String url = properties.getUrlExpression().getValue(message, String.class);
				Object body = null;
				if (properties.getBody() != null) {
					body = properties.getBody();
				}
				else if (properties.getBodyExpression() != null) {
					body = properties.getBodyExpression().getValue(message);
				}
				else {
					body = message.getPayload();
				}
				URI uri = new URI(url);
				RequestEntity<?> request = new RequestEntity<>(body, headers, method, uri);
				ResponseEntity<?> response = restTemplate.exchange(request, responseType);
				return properties.getReplyExpression().getValue(response);
			}
			catch (Exception e) {
				LOG.warn("Error in HTTP request", e);
				return null;
			}
		}

	}

}
