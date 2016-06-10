/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

/**
 * @author David Turanski
 */
@ConfigurationProperties("file.consumer")
public class FileConsumerProperties {

	/**
	 * The FileReadingMode to use for file reading sources.
	 * Values are 'ref' - The File object,
	 * 'lines' - a message per line, or
	 * 'contents' - the contents as bytes.
	 * Default is 'contents'
	 */
	private FileReadingMode fileReadingmode = FileReadingMode.contents;

	/**
	 * 	Set to true to emit start of file/end of file marker messages before/after the data. 
	 * 	Only valid with FileReadingMode 'lines'.
	 */
	private Boolean withMarkers = null;

	@NotNull
	public FileReadingMode getMode() {
		return fileReadingmode;
	}

	public void setMode(FileReadingMode mode) {
		this.fileReadingmode = mode;
	}

	public Boolean getWithMarkers() {
		return withMarkers;
	}

	public void setWithMarkers(Boolean withMarkers) {
		this.withMarkers = withMarkers;
	}

	@AssertTrue(message = "withMarkers can only be supplied when FileReadingMode is 'lines'")
	public boolean isWithMarkersValid() {
		if (this.withMarkers != null && FileReadingMode.lines != this.fileReadingmode) {
			return false;
		}
		else {
			return true;
		}
	}

	@AssertTrue(message = "withMarkers must be set when FileReadingMode is 'lines'")
	public boolean isWithMarkersMustBeSet() {
		if (this.withMarkers == null && FileReadingMode.lines == this.fileReadingmode) {
			return false;
		}
		else {
			return true;
		}
	}

}
