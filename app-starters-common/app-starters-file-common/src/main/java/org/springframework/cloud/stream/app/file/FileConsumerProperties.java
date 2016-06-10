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

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
	private FileReadingMode fileReadingMode = FileReadingMode.contents;

	/**
	 * 	Set to true to emit start of file/end of file marker messages before/after the data.
	 * 	Only valid with FileReadingMode 'lines'.
	 */
	private Boolean withMarkers = null;

	/**
	 * When {@code fileMarkers == true}, specify if they should be produced
	 * as {@link org.springframework.integration.file.splitter.FileSplitter.FileMarker}
	 * objects or JSON.
	 */
	private boolean markersJson = false;

	@NotNull
	public FileReadingMode getMode() {
		return this.fileReadingMode;
	}

	public void setMode(FileReadingMode mode) {
		this.fileReadingMode = mode;
	}

	public Boolean getWithMarkers() {
		return this.withMarkers;
	}

	public void setWithMarkers(Boolean withMarkers) {
		this.withMarkers = withMarkers;
	}

	public boolean getMarkersJson() {
		return this.markersJson;
	}

	public void setMarkersJson(boolean markersJson) {
		this.markersJson = markersJson;
	}

	@AssertTrue(message = "withMarkers can only be supplied when FileReadingMode is 'lines'")
	public boolean isWithMarkersValid() {
		return !(this.withMarkers != null && FileReadingMode.lines != this.fileReadingMode);
	}

}
