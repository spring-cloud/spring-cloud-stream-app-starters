package org.springframework.cloud.stream.app.yahooquotes.source;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;

/**
 * @author Vinicius Carvalho
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(YahooQuotesSourceProperties.class)
public class YahooQuotesSourceConfiguration {

	@Autowired
	private YahooQuotesSourceProperties properties;



	@Autowired
	@Qualifier(Source.OUTPUT)
	private MessageChannel output;

	@Bean
	public YahooQuotesSourceMessageProducer messageProducer() throws Exception{
		YahooQuotesSourceMessageProducer producer = new YahooQuotesSourceMessageProducer();
		producer.setProperties(properties);
		producer.setOutput(output);
		return producer;
	}

}
