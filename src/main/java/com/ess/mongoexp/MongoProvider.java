package com.ess.mongoexp;
import java.util.concurrent.TimeUnit;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoProvider {
    public static MongoDatabase create(AppConfig config) throws Exception {
        MongoClientSettings settings =
            MongoClientSettings.builder()
                .applyConnectionString(
                    new ConnectionString(config.mongodb.uri)
                )
                .retryReads(true)
                .applyToSslSettings(ssl -> {
                    ssl.enabled(true);
                    ssl.invalidHostNameAllowed(false); // true ONLY for debugging
                })
                .applyToSocketSettings(socket -> socket
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                )
                .applyToConnectionPoolSettings(pool -> pool
                    .maxSize(10)
                    .minSize(1)
                    .maxConnectionIdleTime(30, TimeUnit.SECONDS)
                    .maxConnectionLifeTime(2, TimeUnit.MINUTES)
                )
                .applyToServerSettings(server -> server
                    .heartbeatFrequency(60, TimeUnit.SECONDS)
                )
                .build();
        MongoClient client = MongoClients.create(settings);
        return client.getDatabase(config.mongodb.database);
    }
}
