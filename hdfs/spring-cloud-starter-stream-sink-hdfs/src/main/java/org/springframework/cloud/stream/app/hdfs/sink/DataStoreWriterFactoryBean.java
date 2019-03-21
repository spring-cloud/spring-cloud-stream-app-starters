/*
 * Copyright 2014-2015 the original author or authors.
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.hadoop.store.DataStoreWriter;
import org.springframework.data.hadoop.store.codec.CodecInfo;
import org.springframework.data.hadoop.store.codec.Codecs;
import org.springframework.data.hadoop.store.output.PartitionTextFileWriter;
import org.springframework.data.hadoop.store.output.TextFileWriter;
import org.springframework.data.hadoop.store.partition.MessagePartitionStrategy;
import org.springframework.data.hadoop.store.strategy.naming.*;
import org.springframework.data.hadoop.store.strategy.rollover.RolloverStrategy;
import org.springframework.data.hadoop.store.strategy.rollover.SizeRolloverStrategy;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.messaging.Message;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FactoryBean} creating a {@link DataStoreWriter}. Created writer will be either
 * {@link PartitionTextFileWriter} or {@link TextFileWriter} depending whether partition
 * path expression is set.
 *
 * @author Thomas Risberg
 * @author Janne Valkealahti
 * @author Gary Russell
 */
public class DataStoreWriterFactoryBean implements InitializingBean, DisposableBean, FactoryBean<DataStoreWriter<String>>,
		BeanFactoryAware, SmartLifecycle {

	private HdfsSinkProperties properties;

	private volatile DataStoreWriter<String> storeWriter;

	private volatile Configuration configuration;

	private volatile BeanFactory beanFactory;

	private TaskScheduler taskScheduler;

	private TaskExecutor taskExecutor;

	private volatile int phase;

	private volatile boolean autoStartup = true;

	@Override
	public void destroy() throws Exception {
		storeWriter = null;
	}

	@Override
	public DataStoreWriter<String> getObject() throws Exception {
		return storeWriter;
	}

	@Override
	public Class<?> getObjectType() {
		return DataStoreWriter.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Configuration configurationToUse = null;
		if (StringUtils.hasText(properties.getFsUri())) {
			configurationToUse = new Configuration(configuration);
			configurationToUse.set(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY, properties.getFsUri());
		}
		else {
			configurationToUse = configuration;
		}
		EvaluationContext evaluationContext = IntegrationContextUtils.getEvaluationContext(beanFactory);
		CodecInfo codec = null;
		if (properties.getCodec() != null) {
			codec = Codecs.getCodecInfo(properties.getCodec());
		}
		List<FileNamingStrategy> strategies = new ArrayList<>();
		strategies.add(new StaticFileNamingStrategy(properties.getFileName()));
		if (properties.isFileUuid()) {
			strategies.add(new UuidFileNamingStrategy());
		}
		strategies.add(new RollingFileNamingStrategy());
		strategies.add(new StaticFileNamingStrategy(properties.getFileExtension(), "."));
		if (codec != null) {
			strategies.add(new CodecFileNamingStrategy());
		}
		ChainedFileNamingStrategy fileNamingStrategy = new ChainedFileNamingStrategy();
		fileNamingStrategy.setStrategies(strategies);
		RolloverStrategy rolloverStrategy = new SizeRolloverStrategy(properties.getRollover());
		String partitionExpression = null;
		if (StringUtils.hasText(properties.getPartitionPath())) {
			partitionExpression = properties.getPartitionPath();
		}
		Path basePath = new Path(properties.getDirectory());
		if (StringUtils.isEmpty(partitionExpression)) {
			TextFileWriter writer = new TextFileWriter(configurationToUse, basePath, codec);
			writer.setIdleTimeout(properties.getIdleTimeout());
			writer.setCloseTimeout(properties.getCloseTimeout());
			writer.setSyncable(properties.isEnableSync());
			writer.setFlushTimeout(properties.getFlushTimeout());
			if (StringUtils.hasText(properties.getInUsePrefix())) {
				writer.setInWritingPrefix(properties.getInUsePrefix());
			}
			if (StringUtils.hasText(properties.getInUseSuffix())) {
				writer.setInWritingSuffix(properties.getInUseSuffix());
			}
			writer.setOverwrite(properties.isOverwrite());
			writer.setFileNamingStrategy(fileNamingStrategy);
			writer.setRolloverStrategy(rolloverStrategy);
			if (beanFactory != null) {
				writer.setBeanFactory(beanFactory);
			}
			if (properties.getFileOpenAttempts() > 0) {
				writer.setMaxOpenAttempts(properties.getFileOpenAttempts());
			}
			writer.setTaskExecutor(taskExecutor);
			writer.setTaskScheduler(taskScheduler);
			storeWriter = writer;
		}
		else {
			if (!(evaluationContext instanceof StandardEvaluationContext)) {
				throw new RuntimeException("Expecting evaluationContext of type StandardEvaluationContext but was "
						+ evaluationContext);
			}
			MessagePartitionStrategy<String> partitionStrategy = new MessagePartitionStrategy<String>(
					partitionExpression, (StandardEvaluationContext) evaluationContext);
			PartitionTextFileWriter<Message<?>> writer = new PartitionTextFileWriter<Message<?>>(configurationToUse,
					basePath,
					codec,
					partitionStrategy);
			writer.setIdleTimeout(properties.getIdleTimeout());
			writer.setCloseTimeout(properties.getCloseTimeout());
			writer.setSyncable(properties.isEnableSync());
			writer.setFlushTimeout(properties.getFlushTimeout());
			if (StringUtils.hasText(properties.getInUsePrefix())) {
				writer.setInWritingPrefix(properties.getInUsePrefix());
			}
			if (StringUtils.hasText(properties.getInUseSuffix())) {
				writer.setInWritingSuffix(properties.getInUseSuffix());
			}
			writer.setOverwrite(properties.isOverwrite());
			writer.setFileNamingStrategyFactory(fileNamingStrategy);
			writer.setRolloverStrategyFactory(rolloverStrategy);
			if (beanFactory != null) {
				writer.setBeanFactory(beanFactory);
			}
			if (properties.getFileOpenAttempts() > 0) {
				writer.setMaxOpenAttempts(properties.getFileOpenAttempts());
			}
			writer.setTaskExecutor(taskExecutor);
			writer.setTaskScheduler(taskScheduler);
			storeWriter = writer;
		}
		if (storeWriter instanceof InitializingBean) {
			((InitializingBean) storeWriter).afterPropertiesSet();
		}
	}

	@Autowired
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Autowired
	public void setProperties(HdfsSinkProperties properties) {
		this.properties = properties;
	}

	@Autowired
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	@Autowired
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void start() {
		if (storeWriter instanceof Lifecycle) {
			((Lifecycle) storeWriter).start();
		}
	}

	@Override
	public void stop() {
		try {
			storeWriter.close();
		} catch (IOException e) {
			throw new IllegalStateException("Error while closing StoreWriter", e);
		}
		if (storeWriter instanceof Lifecycle) {
			((Lifecycle) storeWriter).stop();
		}
	}

	@Override
	public boolean isRunning() {
		if (storeWriter instanceof Lifecycle) {
			return ((Lifecycle) storeWriter).isRunning();
		}
		else {
			return false;
		}
	}

	@Override
	public boolean isAutoStartup() {
		return autoStartup;
	}

	@Override
	public void stop(Runnable runnable) {
		stop();
		if (runnable != null) {
			runnable.run();
		}
	}

	@Override
	public int getPhase() {
		return phase;
	}
}
