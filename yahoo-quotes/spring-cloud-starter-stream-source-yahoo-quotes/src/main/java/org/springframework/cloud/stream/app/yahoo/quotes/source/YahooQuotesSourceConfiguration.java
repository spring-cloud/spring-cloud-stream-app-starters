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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.yahoo.quotes.source.utils.LoggingErrorHandler;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(YahooQuotesSourceProperties.class)
public class YahooQuotesSourceConfiguration {

	@Autowired
	private YahooQuotesSourceProperties properties;

	@Autowired
	@Qualifier(Source.OUTPUT)
	private MessageChannel output;

	@Bean
	public YahooQuotesSourceMessageProducer messageProducer() throws Exception {
		YahooQuotesSourceMessageProducer producer = new YahooQuotesSourceMessageProducer(
				quotesClient());
		producer.setProperties(properties);
		producer.setOutput(output);
		return producer;
	}

	@Bean
	public YahooQuotesClient quotesClient() throws Exception {
		RestTemplate template = new RestTemplate();
		template.setErrorHandler(new LoggingErrorHandler());
		return new YahooQuotesClient(template);
	}

}
