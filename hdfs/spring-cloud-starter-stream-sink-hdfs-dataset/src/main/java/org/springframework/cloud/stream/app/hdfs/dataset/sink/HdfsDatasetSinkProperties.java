/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.hdfs.dataset.sink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Used to configure the HDFS Dataset Sink module options that are related to writing using Kite SDK.
 *
 * @author Thomas Risberg
 */
@SuppressWarnings("unused")
@ConfigurationProperties("hdfs.dataset")
public class HdfsDatasetSinkProperties {

	/**
	 * The URI to use to access the Hadoop FileSystem.
	 */
	private String fsUri;

	/**
	 * The base directory path where the files will be written in the Hadoop FileSystem.
	 */
	private String directory = "/tmp/hdfs-dataset-sink";

	/**
	 * The sub-directory under the base directory where files will be written.
	 */
	@Value("${spring.application.name:data}")
	private String namespace;

	/**
	 * Threshold in number of messages when file will be automatically flushed and rolled over.
	 */
	private int batchSize = 10000;

	/**
	 * Idle timeout in milliseconds when Hadoop file resource is automatically closed.
	 */
	private long idleTimeout = -1L;

	/**
	 * Whether null property values are allowed, if set to true then schema will use UNION for each field.
	 */
	private boolean allowNullValues = false;

	/**
	 * The format to use, valid options are avro and parquet.
	 */
	private String format = "avro";

	/**
	 * The partition path strategy to use, a list of KiteSDK partition expressions separated by a '/' symbol.
	 */
	private String partitionPath = null;

	/**
	 * The size of the cache to be used for partition writers (10 if omitted).
	 */
	private int writerCacheSize = -1;

	/**
	 * Compression type name (snappy, deflate, bzip2 (avro only) or uncompressed)
	 */
	private String compressionType = "snappy";


	public String getFsUri() {
		return fsUri;
	}

	public void setFsUri(String fsUri) {
		this.fsUri = fsUri;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public boolean isAllowNullValues() {
		return allowNullValues;
	}

	public void setAllowNullValues(boolean allowNullValues) {
		this.allowNullValues = allowNullValues;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getPartitionPath() {
		return partitionPath;
	}

	public void setPartitionPath(String partitionPath) {
		this.partitionPath = partitionPath;
	}

	public int getWriterCacheSize() {
		return writerCacheSize;
	}

	public void setWriterCacheSize(int writerCacheSize) {
		this.writerCacheSize = writerCacheSize;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}
}