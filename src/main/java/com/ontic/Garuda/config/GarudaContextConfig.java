package com.ontic.Garuda.config;

import com.ontic.Garuda.commons.mongo.MongoUtil;
import com.ontic.Garuda.commons.props.PropertyKeys;
import com.ontic.Garuda.commons.props.PropertyReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
public class GarudaContextConfig {

    @Bean
    ReactiveMongoTemplate reactiveMongoTemplate(PropertyReader propertyReader) {
        return new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(MongoUtil.buildMongoClient(propertyReader), propertyReader.readStringProperty(PropertyKeys.Mongo.MONGO_DB_NAME)));
    }
}
