/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.gemfire.source;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.gemfire.JsonObjectTransformer;
import org.springframework.cloud.stream.app.gemfire.config.GemfireClientRegionConfiguration;
import org.springframework.cloud.stream.app.gemfire.config.GemfirePoolConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.gemfire.inbound.CacheListeningMessageProducer;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.MessageChannel;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.pdx.PdxInstance;

/**
 * The Gemfire Source provides a {@link CacheListeningMessageProducer} which produces a
 * message for cache events. Internally it uses a {@link com.gemstone.gemfire.cache.CacheListener}
 * which, by default, emits a native GemFire type which holds all of the event details.
 * This is not ideal for streaming applications because it require this type to
 * be also in the consuming app's classpath. Hence, a SpEl Expression, given by the
 * property 'cacheEventExpression' is used to extract required information from the
 * payload. The default expression is 'newValue' which returns the updated object. This
 * may not be ideal for every use case especially if it does not provide the key
 * value. The key is referenced by the field 'key'. Also available are 'operation' (the
 * operation associated with the event, and 'oldValue'. If the cached key and value
 * types are primitives, an simple expression like "key + ':' + newValue" may work.
 *
 * More complex transformations, such as Json, will require customization. To access
 * the original object, set 'cacheEntryExpression' to '#root' or "#this'.
 *
 * This converts payloads of type {@link PdxInstance}, which Gemfire uses to store
 * JSON content (the type of newValue for instance), to a JSON String.
 *
 *
 * @author David Turanski
 */
@EnableBinding(Source.class)
@Import({ KeyInterestConfiguration.class,
		GemfirePoolConfiguration.class,
		GemfireClientRegionConfiguration.class })
@EnableConfigurationProperties(GemfireSourceProperties.class)
@PropertySource("classpath:gemfire-source.properties")
public class GemfireSourceConfiguration {

	@Autowired
	private GemfireSourceProperties config;

	//NOTE: https://jira.spring.io/browse/SPR-7915 supposedly fixed in SF 4.3. So
	//should be able to change to @Autowired at that point
	@Resource(name = "clientRegion")
	private Region<String, ?> region;

	@Autowired
	@Qualifier(Source.OUTPUT)
	private MessageChannel output;

	@Bean
	public MessageChannel convertToStringChannel(){
		return new DirectChannel();
	}

	@Bean
	public MessageChannel routerChannel(){
		return new DirectChannel();
	}

	@Bean
	PayloadTypeRouter payloadTypeRouter(){
		PayloadTypeRouter router = new PayloadTypeRouter();
		router.setDefaultOutputChannel(output);
		router.setChannelMapping(PdxInstance.class.getName(),"convertToStringChannel");

		return router;
	}

	@Bean
	public IntegrationFlow startFlow() {
		return IntegrationFlows.from(routerChannel())
				.route(payloadTypeRouter())
				.get();
	}

	@Bean JsonObjectTransformer transformer() {
		return new JsonObjectTransformer();
	}

	@Bean IntegrationFlow convertToString() {
		return IntegrationFlows.from(convertToStringChannel())
				.transform(transformer(),"toString")
				.channel(output)
				.get();
	}

	@Bean
	public CacheListeningMessageProducer cacheListeningMessageProducer() {
		CacheListeningMessageProducer cacheListeningMessageProducer = new
				CacheListeningMessageProducer(region);
		cacheListeningMessageProducer.setOutputChannel(routerChannel());
		cacheListeningMessageProducer.setExpressionPayload(
				config.getCacheEventExpression());
		return cacheListeningMessageProducer;
	}

}
