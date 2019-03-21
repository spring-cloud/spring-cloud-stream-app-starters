/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.gemfire.cq.source;

import com.gemstone.gemfire.pdx.PdxInstance;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.gemfire.JsonObjectTransformer;
import org.springframework.cloud.stream.app.gemfire.config.GemfireClientCacheConfiguration;
import org.springframework.cloud.stream.app.gemfire.config.GemfirePoolConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.listener.ContinuousQueryListener;
import org.springframework.data.gemfire.listener.ContinuousQueryListenerContainer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.gemfire.inbound.ContinuousQueryMessageProducer;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.MessageChannel;

/**
 * The Gemfire CQ Source provides a {@link ContinuousQueryMessageProducer} which
 * by default, emits an object of type {@link com.gemstone.gemfire.cache.query.CqEvent}.
 * This is not ideal for streaming applications because it require this type to
 * be also in the consuming app's classpath. Hence, a SpEl Expression, given by the
 * property 'qcEventExpression' is used to extract required information from the
 * payload. The default expression is 'newValue' which returns the updated object. This
 * may not be ideal for every use case especially if it does not provide the key
 * value. The key is referenced by the field 'key'. Also available are the
 * operations associated with the event, and the query. If the cached key and value
 * types are primitives, an simple expression like "key + ':' + newValue" may work.
 *
 * More complex transformations, such as Json, will require customization. To access
 * the original object, set 'cacheEntryExpression' to '#root' or "#this'.
 *
 * This converts payloads of type {@link PdxInstance}, which Gemfire uses to store
 * JSON content (the type of newValue for instance), to a JSON String.
 *
 * @author David Turanski
 */
@EnableBinding(Source.class)
@Import({ GemfireClientCacheConfiguration.class, GemfirePoolConfiguration.class })
@EnableConfigurationProperties(GemfireCqSourceProperties.class)
@PropertySource("gemfire-cq-source.properties")
public class GemfireCqSourceConfiguration {

	@Autowired
	private GemfireCqSourceProperties config;

	@Autowired
	@Qualifier(Source.OUTPUT)
	private MessageChannel output;

	@Autowired
	private ClientCacheFactoryBean clientCache;

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

	@Bean ContinuousQueryListener continuousQueryListener() {
		ContinuousQueryMessageProducer continuousQueryMessageProducer = new
				ContinuousQueryMessageProducer(continuousQueryListenerContainer(),
				config.getQuery());
		continuousQueryMessageProducer.setExpressionPayload(config.getCqEventExpression());
		continuousQueryMessageProducer.setOutputChannel(routerChannel());
		return continuousQueryMessageProducer;
	}


	@Bean
	ContinuousQueryListenerContainer continuousQueryListenerContainer() {
		ContinuousQueryListenerContainer continuousQueryListenerContainer =
				new ContinuousQueryListenerContainer();
		try {
			continuousQueryListenerContainer.setCache(clientCache.getObject());
		}
		catch (Exception e) {
			throw new BeanCreationException(e.getLocalizedMessage(), e);
		}
		return continuousQueryListenerContainer;
	}

}
