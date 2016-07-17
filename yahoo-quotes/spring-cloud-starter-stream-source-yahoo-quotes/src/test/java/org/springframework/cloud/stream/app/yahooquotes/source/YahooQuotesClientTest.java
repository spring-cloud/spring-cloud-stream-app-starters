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

package org.springframework.cloud.stream.app.yahooquotes.source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.web.client.RestTemplate;

/**
 * @author Vinicius Carvalho
 */
public class YahooQuotesClientTest {

	@Test
	public void fetchOneSymbol() throws Exception {
		YahooQuotesClient client = new YahooQuotesClient(new RestTemplate());
		List<Map<String,Object>> results = client.fetchQuotes(Collections.singletonList("AAPL"),null);
		Assert.assertNotNull(results);
		Assert.assertEquals(1,results.size());
		Assert.assertTrue(results.get(0).size() > 1);
	}

	@Test
	public void fetchOneSymbolCustomFields() throws Exception {
		YahooQuotesClient client = new YahooQuotesClient(new RestTemplate());
		List<Map<String,Object>> results = client.fetchQuotes(Collections.singletonList("AAPL"),"symbol,Ask,Bid");
		Assert.assertNotNull(results);
		Assert.assertEquals(1,results.size());
		Assert.assertEquals(3,results.get(0).size());
	}

	@Test
	public void fetchMultipleSymbols() throws Exception {
		YahooQuotesClient client = new YahooQuotesClient(new RestTemplate());
		List<Map<String,Object>> results = client.fetchQuotes(Arrays.asList("AAPL","MSFT"),null);
		Assert.assertNotNull(results);
		Assert.assertEquals(2,results.size());
		Assert.assertTrue(results.get(0).size() > 1);
	}

}
