package com.ontic.Garuda.commons.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.ontic.Garuda.commons.props.PropertyKeys;
import com.ontic.Garuda.commons.props.PropertyReader;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MongoUtil {

    public static MongoClient buildMongoClient(PropertyReader propertyReader) {
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applicationName("GARUDA")
                .applyToConnectionPoolSettings(pool -> {
                    pool.maxSize(propertyReader.readIntegerProperty(PropertyKeys.Mongo.DEFAULT_CONNECTIONS_PER_HOST));
                })
                .applyToSocketSettings(socket -> {
                    socket.connectTimeout(propertyReader.readLongProperty(PropertyKeys.Mongo.DEFAULT_CONNECTION_MAX_IDLE_TIME_MILLIS), TimeUnit.MILLISECONDS);
                    socket.readTimeout(propertyReader.readLongProperty(PropertyKeys.Mongo.DEFAULT_CONNECTION_MAX_IDLE_TIME_MILLIS), TimeUnit.MILLISECONDS);
                }).applyToClusterSettings(cluster -> {
                    String url = propertyReader.readStringProperty(PropertyKeys.Mongo.MONGO_HOST);
                    Integer port = propertyReader.readIntegerProperty(PropertyKeys.Mongo.MONGO_PORT);
                    cluster.hosts(List.of(new ServerAddress(url, port)));
                });
        return MongoClients.create(builder.build());
    }
}
