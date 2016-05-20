/*
 * Copyright 2011-2016 the original author or authors.
 *
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

package org.springframework.cloud.stream.app.metrics.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.springframework.cloud.stream.app.metrics.AggregateCounter;
import org.springframework.cloud.stream.app.metrics.AggregateCounterRepository;
import org.springframework.cloud.stream.app.metrics.AggregateCounterResolution;

/**
 * In-memory aggregate counter with minute resolution.
 * <p/>
 * Note that the data is permanently accumulated, so will grow steadily in size until the host process is restarted.
 *
 * @author Luke Taylor
 * @author Eric Bottard
 * @author Ilayaperumal Gopinathan
 */
public class InMemoryAggregateCounterRepository implements AggregateCounterRepository {

	private Map<String, InMemoryAggregateCounter> aggregates = new ConcurrentHashMap<String, InMemoryAggregateCounter>();

	private Map<String, AggregateCounter> counters = new ConcurrentHashMap<String, AggregateCounter>();


	public long increment(String name) {
		return increment(name, 1L, DateTime.now());
	}

	public long decrement(String name) {
		throw new UnsupportedOperationException("Can't decrement an AggregateCounter");
	}

	@Override
	public void reset(String name) {
		aggregates.remove(name);
		counters.remove(name);
	}

	@Override
	public long increment(String name, long amount, DateTime dateTime) {
		InMemoryAggregateCounter counter = getOrCreate(name);
		return counter.increment(amount, dateTime);
	}

	@Override
	public AggregateCounter getCounts(String name, int nCounts, AggregateCounterResolution resolution) {
		return getOrCreate(name).getCounts(nCounts, new DateTime(), resolution);
	}

	@Override
	public AggregateCounter getCounts(String name, Interval interval, AggregateCounterResolution resolution) {
		return getOrCreate(name).getCounts(interval, resolution);
	}

	@Override
	public AggregateCounter findOne(String name) {
		return counters.get(name);
	}

	@Override
	public List<String> list() {
		List<String> list = new ArrayList<>();
		list.addAll(aggregates.keySet());
		Collections.sort(list);
		return list;
	}

	@Override
	public AggregateCounter getCounts(String name, int nCounts, DateTime end, AggregateCounterResolution resolution) {
		return getOrCreate(name).getCounts(nCounts, end, resolution);
	}

	private synchronized InMemoryAggregateCounter getOrCreate(String name) {
		InMemoryAggregateCounter c = aggregates.get(name);
		if (c == null) {
			c = new InMemoryAggregateCounter(name);
			aggregates.put(name, c);
		}
		return c;
	}

	public AggregateCounter save(AggregateCounter counter) {
		aggregates.remove(counter.getName());
		counters.put(counter.getName(), counter);
		increment(counter.getName(), counter.getTotal(), DateTime.now());
		return counter;
	}

}
