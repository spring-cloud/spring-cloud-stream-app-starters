/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.gpfdist.sink.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime context for load operations.
 *
 * @author Janne Valkealahti
 */
public class RuntimeContext {

	private final List<String> locations;

	/**
	 * Instantiates a new runtime context.
	 */
	public RuntimeContext() {
		this.locations = new ArrayList<>();
	}

	/**
	 * Instantiates a new runtime context.
	 *
	 * @param location the location
	 */
	public RuntimeContext(String location) {
		this.locations = new ArrayList<>();
		addLocation(location);
	}

	/**
	 * Gets the locations.
	 *
	 * @return the locations
	 */
	public List<String> getLocations() {
		return locations;
	}

	/**
	 * Sets the locations.
	 *
	 * @param locations the new locations
	 */
	public void setLocations(List<String> locations) {
		this.locations.clear();
		this.locations.addAll(locations);
	}

	/**
	 * Adds the location.
	 *
	 * @param location the location
	 */
	public void addLocation(String location) {
		this.locations.add(location);
	}
}
