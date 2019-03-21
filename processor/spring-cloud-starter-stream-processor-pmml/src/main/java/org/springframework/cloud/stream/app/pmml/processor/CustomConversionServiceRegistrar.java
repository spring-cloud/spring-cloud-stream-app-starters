/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.stream.app.pmml.processor;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers a DefaultConversionService configured with both an ExpressionConverter and a GenericMapConverter
 * very early in the ApplicationContext lifecycle, so that they get picked up for {@literal @ConfigurationProperties}
 * binding.
 *
 * @author Eric Bottard
 * @author Gary Russell
 */
public class CustomConversionServiceRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME)) {
			BeanDefinitionBuilder conversionService = BeanDefinitionBuilder
					.genericBeanDefinition(CustomDefaultConversionService.class);
			registry.registerBeanDefinition(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, conversionService.getBeanDefinition());

			conversionService.addPropertyReference("spelConverter", "spelConverter");
		}
	}

	public static class CustomDefaultConversionService extends DefaultConversionService {

		public CustomDefaultConversionService() {
			addConverter(new GenericMapConverter(this));
		}

		public void setSpelConverter(SpelExpressionConverterConfiguration.SpelConverter spelConverter) {
			addConverter(spelConverter);
		}

	}

}
