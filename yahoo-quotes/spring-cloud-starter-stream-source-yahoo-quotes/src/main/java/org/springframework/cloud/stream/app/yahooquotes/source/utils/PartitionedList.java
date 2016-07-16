package org.springframework.cloud.stream.app.yahooquotes.source.utils;

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
