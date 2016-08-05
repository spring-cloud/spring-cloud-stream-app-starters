/*
 *  Copyright 2016 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.cloud.stream.app.yahoo.quotes.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = YahooQuotesSourceApplicationTests.TestYahooQuotesSourceApplication.class)
@DirtiesContext
public abstract class YahooQuotesSourceApplicationTests {


	@Autowired
	@Bindings(YahooQuotesSourceConfiguration.class)
	protected Source yahooSource;



	@Autowired
	protected MessageCollector messageCollector;

	@IntegrationTest({"yahoo.quotes.cronExpression=0/1 * * * * *","yahoo.quotes.zone=EST","yahoo.quotes.symbols=GOOGL"})
	public static class ReceiveOneTests extends YahooQuotesSourceApplicationTests {

		@Test
		public void test() throws Exception{
			Message<?> received = messageCollector.forChannel(yahooSource.output()).poll(5, TimeUnit.SECONDS);
			Assert.assertNotNull(received);
		}

	}

	@IntegrationTest({"yahoo.quotes.cronExpression=0/1 * * * * *","yahoo.quotes.zone=EST","yahoo.quotes.symbols=AAPL,GOOGL"})
	public static class ReceiveMultipleTests extends YahooQuotesSourceApplicationTests {

		@Test
		public void test() throws Exception{
			Message<?> received = messageCollector.forChannel(yahooSource.output()).poll(5, TimeUnit.SECONDS);
			Assert.assertNotNull(received);
		}

	}


	@SpringBootApplication
	public static class TestYahooQuotesSourceApplication {

		@Bean
		public YahooQuotesClient quotesClient(){
			return new MockingYahooQuotesClient();
		}
	}

	public static class MockingYahooQuotesClient implements YahooQuotesClient {

		@Override
		public List<Map<String, Object>> fetchQuotes(List<String> symbols, String filter) {
			List<Map<String,Object>> result = new ArrayList<>();
			for(String symbol : symbols){
				Map<String,Object> quote = new HashMap<>();
				quote.put("symbol",symbol);
				quote.putAll(randomFields(filter));
				result.add(quote);
			}
			return result;
		}

		private Map<String,Object> randomFields(String filter){
			Map<String,Object> properties = new HashMap<>();
			Random random = new Random();
			if(StringUtils.isEmpty(filter)){
				properties.put("foo","bar");
			}else{
				String[] fields = filter.split(",");
				for(String field : fields){
					properties.put(field,random.nextInt());
				}
			}

			return properties;
		}
	}
}
