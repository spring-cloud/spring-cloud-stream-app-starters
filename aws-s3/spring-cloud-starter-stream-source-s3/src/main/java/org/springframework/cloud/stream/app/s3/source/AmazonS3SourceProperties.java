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

package org.springframework.cloud.stream.app.s3.source;

import java.io.File;

import org.hibernate.validator.constraints.Length;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.file.remote.AbstractRemoteFileSourceProperties;

/**
 * @author Artem Bilan
 */
@ConfigurationProperties("s3")
public class AmazonS3SourceProperties extends AbstractRemoteFileSourceProperties {

	public AmazonS3SourceProperties() {
		setRemoteDir("bucket");
		setLocalDir(new File(System.getProperty("java.io.tmpdir") + "/s3/source"));
	}

	@Override
	@Length(min = 3)
	public String getRemoteDir() {
		return super.getRemoteDir();
	}

}
