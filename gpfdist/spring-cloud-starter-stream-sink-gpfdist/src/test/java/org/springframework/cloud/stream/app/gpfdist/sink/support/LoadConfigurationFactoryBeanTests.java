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

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link LoadConfigurationFactoryBean}.
 *
 * @author Janne Valkealahti
 */
public class LoadConfigurationFactoryBeanTests {

	@Test
	public void testListValuesToColumns() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"LoadConfigurationFactoryBeanTests1.xml");
		LoadConfigurationFactoryBean factoryBean = context.getBean("&greenplumLoadConfiguration",
				LoadConfigurationFactoryBean.class);
		assertThat(factoryBean.getUpdateColumns().size(), is(2));
		assertThat(factoryBean.getMatchColumns().size(), is(2));
		context.close();
	}

}
