package org.springframework.cloud.stream.app.mongodb.source;

import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.mongodb.inbound.MongoDbMessageSource;
import org.springframework.messaging.MessageChannel;


@EnableBinding(Source.class)
@EnableConfigurationProperties(MongoDBProperties.class)
public class MongoDBSourceConfiguration {

    @Autowired private MongoDBProperties _config;

    @Autowired
    @Qualifier(Source.OUTPUT)
    private MessageChannel output;

    @Bean
    protected MongoTemplate mongoTemplate() {
        try {
            return new MongoTemplate(new SimpleMongoDbFactory(new MongoClientURI(_config.getUri())));
        } catch (Exception ex) {
            throw new BeanCreationException(ex.getMessage(), ex);
        }
    }

    @Bean
    protected MessageSource<Object> mongoSource() {
        MongoDbMessageSource ms = new MongoDbMessageSource(mongoTemplate(), new LiteralExpression(_config.getQuery()));
        ms.setCollectionNameExpression(new LiteralExpression(_config.getCollection()));
        return ms;
    }

    @Bean
    public IntegrationFlow startFlow() throws Exception {
        return IntegrationFlows.from(mongoSource(),
                c -> c.poller(Pollers.fixedRate(_config.getPollingRate())))
                .split()
                .transform(Transformers.toJson())
                .channel(output)
                .get();
    }

}
