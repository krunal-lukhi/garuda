package com.ontic.Garuda.commons.props;

public interface PropertyKeys {

    interface Mongo {
        String MONGO_DB_NAME = "mongo.db.name";
        String MONGO_HOST = "mongo.host";
        String MONGO_PORT = "mongo.port";
        String DEFAULT_CONNECTIONS_PER_HOST = "mongo.default.connections.per.host";
        String DEFAULT_CONNECTION_MAX_IDLE_TIME_MILLIS = "mongo.default.connection.max.idle.time.millis";
    }

}
