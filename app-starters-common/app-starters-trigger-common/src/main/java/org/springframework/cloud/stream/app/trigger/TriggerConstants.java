/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.stream.app.trigger;

/**
 * @author Ilayaperumal Gopinathan
 */
public class TriggerConstants {

	public static final String TRIGGER_BEAN_NAME = "POLLER_TRIGGER";

	public static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";

	public static final String CRON_TRIGGER_OPTION = "trigger.cron";

	public static final String DATE_TRIGGER_OPTION = "trigger.date";

}
