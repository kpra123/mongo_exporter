package com.ess.mongoexp;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoDatabase;
/**
 * Hello world!
 * java -cp .\mongo_exporter-1.0-SNAPSHOT.jar com.ess.mongoexp.App
 */
public class App 
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] _args ) throws Exception
    {
        logger.info("Inside Main Method");
        try {
            logger.info("Before Calling : ConfigLoader.load");
            AppConfig v_AppConfig = ConfigLoader.load(logger);
            logger.info("After Calling : ConfigLoader.load");
            logger.info("Mongodb URL: {}", v_AppConfig.mongodb.uri);
            logger.info("Mongodb Database: {}", v_AppConfig.mongodb.database);
            MongoDatabase database = MongoProvider.create(v_AppConfig);
            MongoCallbackMetricRegistrar.register(database, v_AppConfig);
            //HTTPServer.builder().port(Integer.parseInt(v_AppConfig.prometheus.port)).buildAndStart();
            HTTPServer.Builder builder = HTTPServer.builder().port(Integer.parseInt(v_AppConfig.prometheus.port));
            builder.buildAndStart();
            logger.info("Mongo Prometheus Exporter running on /metrics");
            System.out.println("Mongo Prometheus Exporter running on /metrics");
            Thread.currentThread().join();
       } catch (Exception e) {
            logger.error("A critical issue: {}", e.getStackTrace().toString()); 
            e.printStackTrace();
        }
        logger.info("Exiting Main Method");
    }
}
