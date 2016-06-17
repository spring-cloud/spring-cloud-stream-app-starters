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

package org.springframework.cloud.stream.app.load.generator.source;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.app.test.PropertiesInitializer;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for LoadGeneratorSource.
 *
 * @author Glenn Renfro
 * @author Gary Russell
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LoadGeneratorSourceTests.LoadGeneratorSourceApplication.class,
								initializers = PropertiesInitializer.class)
public class LoadGeneratorSourceTests {

	@Autowired
	@Bindings(LoadGeneratorSourceConfiguration.class)
	protected Source channels;


	@Autowired
	protected MessageCollector messageCollector;

	@BeforeClass
	public static void configureProperties() throws Throwable {

		Properties properties = new Properties();
		properties.put("load-generator.producers", 1);
		properties.put("load-generator.messageSize", 1000);
		properties.put("load-generator.messageCount", 1);
		properties.put("load-generator.generateTimestamp", false);
		PropertiesInitializer.PROPERTIES = properties;
	}

	@Test
	public void testForOneMessage() {
		List<Message<?>> messages = new ArrayList<>();
		assertEquals(1, messageCollector.forChannel(channels.output()).size());
		messageCollector.forChannel(channels.output()).drainTo(messages, 1);
		assertEquals(1, messages.size());
		Message<?> message = messages.get(0);
		byte[] payload = (byte[]) message.getPayload();
		assertEquals(1000,payload.length);
	}

	@SpringBootApplication
	public static class LoadGeneratorSourceApplication {

	}

}
