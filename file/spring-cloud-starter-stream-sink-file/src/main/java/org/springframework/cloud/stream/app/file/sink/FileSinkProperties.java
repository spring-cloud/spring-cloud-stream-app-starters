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

package org.springframework.cloud.stream.app.file.sink;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import java.io.File;

/**
 * Properties for the file sink.
 *
 * @author Mark Fisher
 */
@ConfigurationProperties("file")
public class FileSinkProperties {

	private static final String DEFAULT_DIR = System.getProperty("java.io.tmpdir") +
			File.separator + "dataflow" + File.separator + "output";

	private static final String DEFAULT_NAME = "file-sink";

	/**
	 * A flag to indicate whether content should be written as bytes.
	 */
	private boolean binary = false;

	/**
	 * The charset to use when writing text content.
	 */
	private String charset = "UTF-8";

	/**
	 * The parent directory of the target file.
	 */
	private String directory = DEFAULT_DIR;

	/**
	 * The expression to evaluate for the parent directory of the target file.
	 */
	private Expression directoryExpression;

	/**
	 * The FileExistsMode to use if the target file already exists.
	 */
	private FileExistsMode mode = FileExistsMode.APPEND;

	/**
	 * The name of the target file.
	 */
	private String name = DEFAULT_NAME;

	/**
	 * The expression to evaluate for the name of the target file.
	 */
	private String nameExpression;

	/**
	 * The suffix to append to file name.
	 */
	private String suffix = "";

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Expression getDirectoryExpression() {
		return directoryExpression;
	}

	public void setDirectoryExpression(Expression directoryExpression) {
		this.directoryExpression = directoryExpression;
	}

	public FileExistsMode getMode() {
		return mode;
	}

	public void setMode(FileExistsMode mode) {
		this.mode = mode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameExpression() {
		return (nameExpression != null)
				? nameExpression + " + '" + getSuffix() + "'"
				: "'" + name + getSuffix() + "'";
	}

	public void setNameExpression(String nameExpression) {
		this.nameExpression = nameExpression;
	}

	public String getSuffix() {
		String suffixWithDotIfNecessary = "";
		if (StringUtils.hasText(suffix)) {
			suffixWithDotIfNecessary = suffix.startsWith(".") ? suffix : "." + suffix;
		}
		return suffixWithDotIfNecessary;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@AssertTrue(message = "Exactly one of 'name' or 'nameExpression' must be set")
	public boolean isMutuallyExclusiveNameAndNameExpression() {
		return DEFAULT_NAME.equals(name) || nameExpression == null;
	}

	@AssertTrue(message = "Exactly one of 'directory' or 'directoryExpression' must be set")
	public boolean isMutuallyExclusiveDirectoryAndDirectoryExpression() {
		return DEFAULT_DIR.equals(directory) || directoryExpression == null;
	}
}
