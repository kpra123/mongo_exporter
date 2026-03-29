package com.ess.mongoexp;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoProvider {
    public static MongoDatabase create(AppConfig config) throws Exception {
        String template = "mongodb://%s:%s@%s/sample-database?ssl=true&replicaSet=rs0&readpreference=%s";
        String connectionString = String.format(template, config.mongodb.username, config.mongodb.password, config.mongodb.clusterEndpoint, config.mongodb.readPreference);
        System.setProperty("javax.net.ssl.trustStore", config.mongodb.truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", config.mongodb.truststorePassword);
        /*
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
         */
        MongoClient client = MongoClients.create(connectionString);
        return client.getDatabase(config.mongodb.database);
    }
}
