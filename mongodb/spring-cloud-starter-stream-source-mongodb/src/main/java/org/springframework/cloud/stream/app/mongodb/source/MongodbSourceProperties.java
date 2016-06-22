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

package org.springframework.cloud.stream.app.mongodb.source;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Adam Zwickey
 *
 */
@ConfigurationProperties("mongodb")
public class MongodbSourceProperties {

    /**
     * The MongoDB collection to query
     */
    private String collection;
    /**
     * The MongoDB query
     */
    private String query = "{ }";
    /**
     * Whether to split the query result as individual messages.
     */
    private boolean split = true;

    @NotEmpty(message = "Query is required")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    @NotBlank(message = "Collection name is required")
    public String getCollection() {
        return collection;
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }
}
