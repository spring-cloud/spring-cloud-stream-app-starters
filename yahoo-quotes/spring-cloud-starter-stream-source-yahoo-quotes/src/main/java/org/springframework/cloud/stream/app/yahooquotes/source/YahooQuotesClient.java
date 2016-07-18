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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Vinicius Carvalho
 */
public class YahooQuotesClient {

	private final String endpoint = "https://query.yahooapis.com/v1/public/yql?q={query}&format=json&env={env}&callback=";

	private final String queryTemplate = "select %s from yahoo.finance.quotes(%d) where symbol in (%s)";

	private RestTemplate client;

	private ObjectMapper mapper = new ObjectMapper();

	public YahooQuotesClient(RestTemplate client) {
		this.client = client;
	}

	public List<Map<String, Object>> fetchQuotes(List<String> symbols, String filter) {
		String fields = (StringUtils.isEmpty(filter)) ? "*" : filter;
		String query = String.format(queryTemplate, fields, symbols.size(),wouldbeSimplerWithLambdas(symbols));
		Map<String, String> vars = new HashMap<>();
		vars.put("query", query);
		vars.put("env", "store://datatables.org/alltableswithkeys");
		ResponseEntity<JsonNode> response = client.getForEntity(endpoint, JsonNode.class,
				vars);
		return processResponse(response);
	}

	private List<Map<String, Object>> processResponse(ResponseEntity<JsonNode> response) {
		if (!response.getStatusCode().is2xxSuccessful())
			return Collections.emptyList();
		JsonNode node = response.getBody();
		JsonNode resultsNode = node.get("query").get("results").get("quote");
		List<Map<String, Object>> results = new LinkedList<>();
		if (resultsNode.isArray()) {
			for (JsonNode child : resultsNode) {
				results.add(mapper.convertValue(child, Map.class));
			}
		}
		else {
			results.add(mapper.convertValue(resultsNode, Map.class));
		}
		return results;
	}

	private String wouldbeSimplerWithLambdas(List<String> symbols){
		for(int i=0;i<symbols.size();i++){
			symbols.set(i,StringUtils.quote(symbols.get(i)));
		}
		return StringUtils.collectionToCommaDelimitedString(symbols);
	}
}
