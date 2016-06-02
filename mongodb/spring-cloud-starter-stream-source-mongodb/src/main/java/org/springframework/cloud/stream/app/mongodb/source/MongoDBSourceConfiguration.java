/*
 * Copyright (c) 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License") ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.mongodb.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.app.trigger.TriggerConfiguration;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.mongodb.inbound.MongoDbMessageSource;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;

/**
 * @author Adam Zwickey
 *
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(MongoDBSourceProperties.class)
@Import({ TriggerConfiguration.class, MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MongoDBSourceConfiguration {

    @Autowired
    private MongoDBSourceProperties config;

    @Autowired
    @Qualifier("defaultPoller")
    private PollerMetadata poller;

    @Autowired
    @Qualifier(Source.OUTPUT)
    private MessageChannel output;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Bean
    protected MessageSource<Object> mongoSource() {
        MongoDbMessageSource ms = new MongoDbMessageSource(mongoTemplate, new LiteralExpression(config.getQuery()));
        ms.setCollectionNameExpression(new LiteralExpression(config.getCollection()));
        ms.setEntityClass(String.class);
        return ms;
    }

    @Bean
    public IntegrationFlow startFlow() throws Exception {
        IntegrationFlowBuilder flow =  IntegrationFlows.from(mongoSource(),
                    new Consumer<SourcePollingChannelAdapterSpec>() {
                        @Override
                        public void accept(SourcePollingChannelAdapterSpec sourcePollingChannelAdapterSpec) {
                            sourcePollingChannelAdapterSpec.poller(poller);
                        }
                    });
        if (config.isSplit()) {
            flow.split();
        }
        flow.channel(output);
        return flow.get();
    }

}
