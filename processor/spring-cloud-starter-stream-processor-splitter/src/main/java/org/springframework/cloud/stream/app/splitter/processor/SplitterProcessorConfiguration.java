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

package org.springframework.cloud.stream.app.splitter.processor;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.Expression;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.config.AbstractSimpleMessageHandlerFactoryBean;
import org.springframework.integration.file.splitter.FileSplitter;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.integration.splitter.ExpressionEvaluatingSplitter;

/**
 * A Processor app that splits messages into component parts. Messages with
 * {@code Iterable} payloads need no configuration. Messages with String payloads
 * need 'delimiters' to be specified. Messages with {@code File} payloads need
 * 'charset' and/or 'fileMarker' properties. Mixed payload types are not supported
 * (with the exception that the `String` and `Collection` can be split by a single
 * splitter).
 *
 * @author Gary Russell
 */
@EnableBinding(Processor.class)
@EnableConfigurationProperties(SplitterProcessorProperties.class)
public class SplitterProcessorConfiguration {

	@Autowired
	@Bindings(SplitterProcessorConfiguration.class)
	private Processor channels;

	@Bean
	@Splitter(inputChannel = Processor.INPUT)
	public AbstractSimpleMessageHandlerFactoryBean<AbstractMessageSplitter> splitterHandler(
			final SplitterProcessorProperties properties) {
		return new AbstractSimpleMessageHandlerFactoryBean<AbstractMessageSplitter>() {

			/*
			 * Returns a DefaultMessageSplitter, ExpressionEvaluatingMessageSplitter
			 * or iterator-based FileSplitter depending on properties.
			 * TODO: INT-3920 (SI 4.3) - subclass SplitterFactoryBean instead.
			 */

			@Override
			protected AbstractMessageSplitter createHandler() {
				AbstractMessageSplitter splitterHandler;
				Expression expression = properties.getExpression();
				if (expression != null) {
					splitterHandler = new ExpressionEvaluatingSplitter(expression);
				}
				else {
					String charset = properties.getCharset();
					Boolean markers = properties.getFileMarkers();
					if(markers != null || charset != null) {
						if (markers == null) {
							markers = false;
						}
						FileSplitter splitter = new FileSplitter(true, markers, properties.getMarkersJson());
						if (charset != null) {
							splitter.setCharset(Charset.forName(charset));
						}
						splitterHandler = splitter;
					}
					else {
						DefaultMessageSplitter splitter = new DefaultMessageSplitter();
						splitter.setDelimiters(properties.getDelimiters());
						splitterHandler = splitter;
					}
				}
				splitterHandler.setOutputChannel(channels.output());
				splitterHandler.setApplySequence(properties.isApplySequence());
				return splitterHandler;
			}

		};
	}

}
