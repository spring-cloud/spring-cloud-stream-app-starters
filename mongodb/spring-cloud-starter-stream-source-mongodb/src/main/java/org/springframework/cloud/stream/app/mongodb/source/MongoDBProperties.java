package org.springframework.cloud.stream.app.mongodb.source;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
