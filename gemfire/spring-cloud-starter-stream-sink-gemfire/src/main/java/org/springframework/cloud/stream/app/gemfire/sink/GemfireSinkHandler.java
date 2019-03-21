/*
 * Copyright (c) 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License") ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gemfire.sink;

import com.gemstone.gemfire.pdx.PdxInstance;
import org.springframework.cloud.stream.app.gemfire.JsonObjectTransformer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.MessageConversionException;

/**
 * @author David Turanski
 **/
class GemfireSinkHandler {
	private final MessageHandler messageHandler;
	private final Boolean convertToJson;
	private final JsonObjectTransformer transformer = new JsonObjectTransformer();

	GemfireSinkHandler(MessageHandler messageHandler, Boolean convertToJson){
		this.messageHandler = messageHandler;
		this.convertToJson = convertToJson;
	}
	public void handle(Message<?> message){
		Message<?> transformedMessage = message;
		if (convertToJson) {
			Object payload = message.getPayload();

			if (payload instanceof String) {
				PdxInstance transformedPayload = transformer.toObject((String)payload);
				transformedMessage =  MessageBuilder
						.fromMessage(message)
						.withPayload(transformedPayload)
						.build();
			}
			else {
				throw new MessageConversionException(String.format(
						"Cannot convert object of type %s", payload.getClass()
								.getName()));
			}
		}
		messageHandler.handleMessage(transformedMessage);
	}
}
