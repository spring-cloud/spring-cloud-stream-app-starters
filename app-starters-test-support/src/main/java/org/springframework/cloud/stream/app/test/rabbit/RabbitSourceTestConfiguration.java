/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.test.rabbit;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.stream.app.test.BinderTestPropertiesInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.Channel;

/**
 * Generated app test configuration for RabbitMQ Source.
 *
 * @author Gary Russell
 *
 */
@Configuration
public class RabbitSourceTestConfiguration {

	@Bean
	public static BinderTestPropertiesInitializer loadProps(ConfigurableApplicationContext context) {
		// minimal properties for the context to load
		Properties properties = new Properties();
		properties.put("queues", "foo");
		return new BinderTestPropertiesInitializer(context, properties);
	}

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		ConnectionFactory mockCF = mock(ConnectionFactory.class);
		Connection mockConn = mock(Connection.class);
		when(mockCF.createConnection()).thenReturn(mockConn);
		Channel mockChannel = mock(Channel.class);
		when(mockChannel.isOpen()).thenReturn(true);
		when(mockConn.createChannel(anyBoolean())).thenReturn(mockChannel);
		return mockCF;
	}

}
