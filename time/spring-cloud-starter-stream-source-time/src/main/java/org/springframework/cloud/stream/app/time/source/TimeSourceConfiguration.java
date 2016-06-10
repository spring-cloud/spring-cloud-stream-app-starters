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

package org.springframework.cloud.stream.app.time.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.annotation.PollableSource;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.app.trigger.TriggerProperties;
import org.springframework.cloud.stream.app.trigger.TriggerPropertiesMaxMessagesDefaultOne;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Date;

/**
 * @author Dave Syer
 * @author Glenn Renfro
 * @author Marius Bogoevici
 */
@EnableBinding(Source.class)
@Import(TriggerConfiguration.class)
public class TimeSourceConfiguration {

	@Autowired
	private TriggerProperties triggerProperties;

	@Bean
	public TriggerProperties triggerProperties() {
		return new TriggerPropertiesMaxMessagesDefaultOne();
	}

	@PollableSource
	public String publishTime() {
		return this.triggerProperties.getDateFormat().format(new Date());
	}

}
