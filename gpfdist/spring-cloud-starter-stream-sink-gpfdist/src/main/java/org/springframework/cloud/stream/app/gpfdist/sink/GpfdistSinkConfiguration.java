/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gpfdist.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.gpfdist.sink.support.*;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hadoop.util.net.DefaultHostInfoDiscovery;
import org.springframework.data.hadoop.util.net.HostInfoDiscovery;
import org.springframework.data.hadoop.util.net.HostInfoDiscovery.HostInfo;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Configuration for all beans needed for gpfdist sink.
 *
 * @author Janne Valkealahti
 */
@Configuration
@EnableConfigurationProperties({ GpfdistSinkProperties.class, HostInfoDiscoveryProperties.class })
@EnableBinding(Sink.class)
public class GpfdistSinkConfiguration {

	@Autowired
	private GpfdistSinkProperties properties;

	@Autowired
	private HostInfoDiscoveryProperties discoveryProperties;

	@Bean
	public HostInfoDiscovery hostInfoDiscovery() {
		DefaultHostInfoDiscovery discovery = new DefaultHostInfoDiscovery();
		if (StringUtils.hasText(discoveryProperties.getMatchIpv4())) {
			discovery.setMatchIpv4(discoveryProperties.getMatchIpv4());
		}
		if (StringUtils.hasText(discoveryProperties.getMatchInterface())) {
			discovery.setMatchInterface(discoveryProperties.getMatchInterface());
		}
		if (discoveryProperties.getPreferInterface() != null) {
			discovery.setPreferInterface(discoveryProperties.getPreferInterface());
		}
		discovery.setLoopback(discoveryProperties.isLoopback());
		discovery.setPointToPoint(discoveryProperties.isPointToPoint());
		return discovery;
	}

	@Bean
	public TaskScheduler sqlTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		taskScheduler.setAwaitTerminationSeconds(properties.getBatchTimeout());
		return taskScheduler;
	}

	@Bean
	public ControlFileFactoryBean greenplumControlFile() {
		ControlFileFactoryBean factoryBean = new ControlFileFactoryBean();
		factoryBean.setControlFileResource(properties.getControlFile());
		return factoryBean;
	}

	@Bean
	public GreenplumDataSourceFactoryBean dataSource(ControlFile controlFile) {
		GreenplumDataSourceFactoryBean factoryBean = new GreenplumDataSourceFactoryBean();
		factoryBean.setControlFile(controlFile);
		factoryBean.setDbHost(properties.getDbHost());
		factoryBean.setDbName(properties.getDbName());
		factoryBean.setDbUser(properties.getDbUser());
		factoryBean.setDbPassword(properties.getDbPassword());
		factoryBean.setDbPort(properties.getDbPort());
		return factoryBean;
	}

	@Bean
	public ReadableTableFactoryBean greenplumReadableTable(ControlFile controlFile, HostInfoDiscovery hostInfoDiscovery) {
		ReadableTableFactoryBean factoryBean = new ReadableTableFactoryBean();
		factoryBean.setControlFile(controlFile);
		factoryBean.setDelimiter(properties.getColumnDelimiter());
		factoryBean.setLogErrorsInto(properties.getErrorTable());
		factoryBean.setSegmentReject(properties.getSegmentRejectLimit());
		factoryBean.setSegmentRejectType(properties.getSegmentRejectType());
		factoryBean.setNullString(properties.getNullString());
		HostInfo hostInfo = hostInfoDiscovery.getHostInfo();
		factoryBean.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri(hostInfo.getAddress(), properties.getGpfdistPort())));
		return factoryBean;
	}

	@Bean
	public LoadConfigurationFactoryBean greenplumLoadConfiguration(ReadableTable externalTable, ControlFile controlFile) {
		LoadConfigurationFactoryBean factoryBean = new LoadConfigurationFactoryBean();
		factoryBean.setExternalTable(externalTable);
		factoryBean.setControlFile(controlFile);
		factoryBean.setMode(StringUtils.hasText(properties.getMode()) ? Mode.valueOf(properties.getMode().toUpperCase()) : Mode.INSERT);
		factoryBean.setUpdateColumns(StringUtils.commaDelimitedListToStringArray(properties.getUpdateColumns()));
		factoryBean.setMatchColumns(StringUtils.commaDelimitedListToStringArray(properties.getMatchColumns()));
		factoryBean.setTable(properties.getTable());
		factoryBean.setSqlBefore(StringUtils.hasText(properties.getSqlBefore()) ? Arrays.asList(properties.getSqlBefore()) : new ArrayList<String>());
		factoryBean.setSqlAfter(StringUtils.hasText(properties.getSqlAfter()) ? Arrays.asList(properties.getSqlAfter()) : new ArrayList<String>());
		return factoryBean;
	}

	@Bean
	public LoadFactoryBean greenplumLoad(LoadConfiguration loadConfiguration, DataSource dataSource) {
		LoadFactoryBean factoryBean = new LoadFactoryBean();
		factoryBean.setLoadConfiguration(loadConfiguration);
		factoryBean.setDataSource(dataSource);
		return factoryBean;
	}

	@Bean
	@ServiceActivator(inputChannel= Sink.INPUT)
	public GpfdistMessageHandler gpfdist(GreenplumLoad greenplumLoad, TaskScheduler sqlTaskScheduler, HostInfoDiscovery hostInfoDiscovery) {
		GpfdistMessageHandler handler = new GpfdistMessageHandler(properties.getGpfdistPort(), properties.getFlushCount(),
				properties.getFlushTime(), properties.getBatchTimeout(), properties.getBatchCount(), properties.getBatchPeriod(),
				properties.getDelimiter(), hostInfoDiscovery);
		handler.setRateInterval(properties.getRateInterval());
		handler.setGreenplumLoad(greenplumLoad);
		handler.setSqlTaskScheduler(sqlTaskScheduler);
		return handler;
	}
}
