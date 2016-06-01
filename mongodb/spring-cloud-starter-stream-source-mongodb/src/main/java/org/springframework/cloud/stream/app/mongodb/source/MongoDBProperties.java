/*
 * Copyright (c) 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License") ;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.mongodb.source;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Adam Zwickey
 *
 */
@ConfigurationProperties
public class MongoDBProperties {

    private String _uri, _collection, _exp;
    private long _pollingRate = 10000;

    @NotEmpty(message = "Query is required")
    public String getQuery() {
        return _exp;
    }

    public void setQuery(String query) {
        _exp = query;
    }

    @NotEmpty(message = "MongoDB URI is required")
    public String getUri() {
        return _uri;
    }

    public void setUri(String uri) {
        _uri = uri;
    }

    public void setCollection(String collection) {
        _collection = collection;
    }

    @NotBlank(message = "Collection name is required")
    public String getCollection() {
        return _collection;
    }

    public long getPollingRate() {
        return _pollingRate;
    }

    public void setPollingRate(long pollingRate) {
        _pollingRate = pollingRate;
    }
}
