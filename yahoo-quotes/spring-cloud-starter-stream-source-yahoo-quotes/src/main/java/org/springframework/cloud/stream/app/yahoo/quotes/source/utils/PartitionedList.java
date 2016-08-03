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

package org.springframework.cloud.stream.app.yahoo.quotes.source.utils;

import java.util.AbstractList;
import java.util.List;

/**
 * @author Vinicius Carvalho
 * Taken from http://svn.apache.org/viewvc/commons/proper/collections/trunk/src/main/java/org/apache/commons/collections4/ListUtils.java
 */
public class PartitionedList<T> extends AbstractList<List<T>> {

	final List<T> list;
	private final int size;

	public PartitionedList(List<T> list, int size) {
		if (list == null) {
			throw new NullPointerException("List must not be null");
		}
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be greater than 0");
		}
		this.list = list;
		this.size = size;
	}

	@Override
	public List<T> get(int index) {
		final int listSize = size();
		if (listSize < 0) {
			throw new IllegalArgumentException("negative size: " + listSize);
		}
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index " + index + " must not be negative");
		}
		if (index >= listSize) {
			throw new IndexOutOfBoundsException("Index " + index + " must be less than size " + listSize);
		}
		final int start = index * size;
		final int end = Math.min(start + size, list.size());
		return list.subList(start, end);
	}

	@Override
	public int size() {
		return (list.size() + size - 1) / size;
	}
}
