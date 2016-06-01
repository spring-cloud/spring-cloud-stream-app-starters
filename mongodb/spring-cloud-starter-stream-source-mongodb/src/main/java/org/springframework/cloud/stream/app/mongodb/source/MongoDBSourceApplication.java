package org.springframework.cloud.stream.app.mongodb.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;;

@SpringBootApplication
@EnableAutoConfiguration
public class MongoDBSourceApplication {

    protected final Logger LOG = LoggerFactory.getLogger(MongoDBSourceConfiguration.class);

    public static void main(String[] args) {
        SpringApplication.run(MongoDBSourceApplication.class, args);
    }
}
