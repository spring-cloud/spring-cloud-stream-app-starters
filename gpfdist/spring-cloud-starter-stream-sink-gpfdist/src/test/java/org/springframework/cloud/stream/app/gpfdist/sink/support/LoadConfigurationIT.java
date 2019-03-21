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
package org.springframework.cloud.stream.app.gpfdist.sink.support;

import org.junit.After;
import org.junit.Test;
import org.springframework.cloud.stream.app.gpfdist.sink.AbstractDbTests;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class LoadConfigurationIT extends AbstractDbTests {

	public final String TEST_TABLE = "LoadConfigurationIT";

	@Test
	public void testErrorTableSql() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setLogErrorsInto("myerror");
		factory1.setSegmentRejectLimit(2);
		factory1.setSegmentRejectType(SegmentRejectType.ROWS);
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("LOG ERRORS INTO myerror SEGMENT REJECT LIMIT 2 ROWS"));
		assertSql(sql);
	}

	@Test
	public void testErrorSegmentRejectPercent1() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setLogErrorsInto("myerror");
		factory1.setSegmentRejectLimit(2);
		factory1.setSegmentRejectType(SegmentRejectType.PERCENT);
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("LOG ERRORS INTO myerror SEGMENT REJECT LIMIT 2 PERCENT"));
		assertSql(sql);
	}

	@Test
	public void testErrorSegmentRejectPercent2() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setLogErrorsInto("myerror");
		factory1.setSegmentReject("3%");
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("LOG ERRORS INTO myerror SEGMENT REJECT LIMIT 3 PERCENT"));
		assertSql(sql);
	}

	@Test
	public void testErrorSegmentRejectPercent3() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setLogErrorsInto("myerror");
		factory1.setSegmentRejectLimit(2);
		factory1.setSegmentRejectType(SegmentRejectType.ROWS);
		// 3% overrides manually set 2 and ROWS
		factory1.setSegmentReject("3%");
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("LOG ERRORS INTO myerror SEGMENT REJECT LIMIT 3 PERCENT"));
		assertSql(sql);
	}

	@Test
	public void testNullString1() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setNullString("nullstring");
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("NULL 'nullstring'"));
		assertSql(sql);
	}

	@Test
	public void testNullString2() throws Exception {
		context.register(TestDatasourceConfig.class);
		context.refresh();
		dropTestTable();
		createTestTable();

		ReadableTableFactoryBean factory1 = new ReadableTableFactoryBean();
		factory1.setLocations(Arrays.asList(NetworkUtils.getGPFDistUri("localhost", 1234)));
		factory1.setNullString("\\'\\'");
		factory1.afterPropertiesSet();

		LoadConfigurationFactoryBean factory2 = new LoadConfigurationFactoryBean();
		factory2.setExternalTable(factory1.getObject());
		factory2.setTable("LoadConfigurationIT");
		factory2.afterPropertiesSet();
		LoadConfiguration loadConfiguration = factory2.getObject();

		String sql = SqlUtils.createExternalReadableTable(loadConfiguration, "xxx", null);
		assertThat(sql, containsString("NULL '\\'\\''"));
		assertSql(sql);
	}

	private void assertSql(String sql) {
		JdbcTemplate template = context.getBean(JdbcTemplate.class);
		template.execute(sql);
	}

	private void createTestTable() {
		JdbcTemplate template = context.getBean(JdbcTemplate.class);
		template.execute("create table " + TEST_TABLE + " (data text)");
	}

	private void dropTestTable() {
		JdbcTemplate template = context.getBean(JdbcTemplate.class);
		template.execute("drop table if exists " + TEST_TABLE);
		template.execute("drop external table if exists " + TEST_TABLE + "_ext_xxx");
	}

	@After
	public void clean() {
		dropTestTable();
		super.clean();
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
