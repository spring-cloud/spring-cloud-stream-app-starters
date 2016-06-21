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

package org.springframework.cloud.stream.app.file.source;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.AssertTrue;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Properties for the file source.
 *
 * @author Gary Russell
 */
@ConfigurationProperties("file")
public class FileSourceProperties {

	private static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir") +
			File.separator + "dataflow" + File.separator + "input";

	/**
	 * The directory to poll for new files.
	 */
	private String directory = DEFAULT_DIR;

	/**
	 * Set to true to include an AcceptOnceFileListFilter which prevents duplicates.
	 */
	private boolean preventDuplicates = true;

	/**
	 * A simple ant pattern to match files.
	 */
	private String filenamePattern;

	/**
	 * A regex pattern to match files.
	 */
	private Pattern filenameRegex;

	public String getDirectory() {
		return this.directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public boolean isPreventDuplicates() {
		return this.preventDuplicates;
	}

	public void setPreventDuplicates(boolean preventDuplicates) {
		this.preventDuplicates = preventDuplicates;
	}

	public String getFilenamePattern() {
		return this.filenamePattern;
	}

	public void setFilenamePattern(String filenamePattern) {
		this.filenamePattern = filenamePattern;
	}

	public Pattern getFilenameRegex() {
		return this.filenameRegex;
	}

	public void setFilenameRegex(Pattern filenameRegex) {
		this.filenameRegex = filenameRegex;
	}

	@AssertTrue(message = "filenamePattern and filenameRegex are mutually exclusive")
	public boolean isExclusivePatterns() {
		return !(this.filenamePattern != null && this.filenameRegex != null);
	}

}
