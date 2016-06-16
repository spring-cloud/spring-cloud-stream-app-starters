package org.springframework.cloud.stream.app.test.aggregate.counter;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.analytics.metrics.AggregateCounterRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Soby Chacko
 */
@Configuration
public class AggregateCounterSinkTestConfiguration {

	@Bean
	@ConditionalOnClass(AggregateCounterRepository.class)
	public AggregateCounterRepository aggregateCounterRepository() {
		return Mockito.mock(AggregateCounterRepository.class);
	}

}
