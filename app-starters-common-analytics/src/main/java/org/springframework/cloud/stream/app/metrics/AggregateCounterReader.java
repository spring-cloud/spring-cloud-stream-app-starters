/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.cloud.stream.app.metrics;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Collection;
import java.util.List;

/**
 * Contains operations to find AggreageCounters.
 *
 * @author Luke Taylor
 * @author Ilayaperumal Gopinathan
 */
public interface AggregateCounterReader {

	/**
	 * Query function which returns the last 'n' points for a particular resolution.
	 *
	 * @param name the counter to query
	 * @param nCounts the number of data points to return
	 * @param resolution the resolution at which the data should be returned (minute, hour, day, month)
	 * @return an object containing an indexed array of the aggregate counts for the given query.
	 */
	AggregateCounter getCounts(String name, int nCounts, AggregateCounterResolution resolution);

	/**
	 * Query function to allow the counts for a specific interval to be retrieved for the given resolution.
	 *
	 *
	 * @param name the counter to query
	 * @param interval the time interval to return data for. Includes start and end.
	 * @param resolution the resolution at which the data should be returned (minute, hour, day, month)
	 * @return an object containing an indexed array of the aggregate counts for the given query.
	 */
	AggregateCounter getCounts(String name, Interval interval, AggregateCounterResolution resolution);

	/**
	 * Queries by requesting a number of points, ending on the given date (inclusive).
	 * The date may be null to use the current time, thus returning the last nCounts point
	 * at the given resolution.
	 *
	 * @param name the counter to query
	 * @param end the end of the query interval (inclusive). Cannot be null.
	 * @param nCounts the number of data points to return
	 * @param resolution the resolution at which the data should be returned (minute, hour, day, month)
	 * @return an object containing an indexed array of the counts .
	 */
	AggregateCounter getCounts(String name, int nCounts, DateTime end, AggregateCounterResolution resolution);

	/**
	 * Retrieve a single counter by name.
	 */
	AggregateCounter findOne(String name);

	/**
	 * List the names of all available aggregate counters.
	 */
	List<String> list();
}
