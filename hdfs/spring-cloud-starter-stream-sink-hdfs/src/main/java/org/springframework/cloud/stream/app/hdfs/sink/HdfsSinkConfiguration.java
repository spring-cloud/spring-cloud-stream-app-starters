/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.hdfs.sink;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.hadoop.store.DataStoreWriter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuration class for the HdfsSink. Delegates to a {@link DataStoreWriterFactoryBean} for
 * creating the writer used by the sink.
 * <p/>
 * The configuration contains the property 'fsUri' to configure a connection to HDFS as well as the
 * additional properties for the sink like directory, fileName, codec etc. You can also use the
 * standard 'spring.hadoop.fsUri' property for specifying the HDFS connection.
 *
 * @author Thomas Risberg
 */
@EnableBinding(Sink.class)
@EnableConfigurationProperties(HdfsSinkProperties.class)
public class HdfsSinkConfiguration {

	private DataStoreWriter<String> dataStoreWriter;

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	@Primary
	public TaskExecutor taskExecutor() {
		return new ThreadPoolTaskExecutor();
	}

	@Bean
	public DataStoreWriterFactoryBean dataStoreWriter() {
		return new DataStoreWriterFactoryBean();
	}

	@Autowired
	public void setDataStoreWriter(DataStoreWriter<String> dataStoreWriter) {
		this.dataStoreWriter = dataStoreWriter;
	}

	@ServiceActivator(inputChannel=Sink.INPUT)
	public void hdfsSink(Object payload) {
		try {
			dataStoreWriter.write(payload.toString());
		} catch (IOException e) {
			throw new IllegalStateException("Error while writing", e);
		}
	}
}