package com.ess.mongoexp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.BsonArray;
import org.bson.Document;
import org.slf4j.Logger;

public class MongoCallbackMetricRegistrar {
    //private final CollectorRegistry registry = new CollectorRegistry();
    //private final PushGateway pushGateway;
    public static void register(Logger logger, MongoDatabase database, AppConfig config){
        //MongoDatabase database, List<MetricConfig> metrics
        for (MetricConfig metric : config.metrics) {
            MetricConfig metricCopy = metric;
            logger.info("Mitric Name" + metric.name);
            MongoCollection<Document> collection = database.getCollection(metric.collection);
            //String[] labelNames = metric.staticlabels.keySet().toArray(String[]::new);
            //String[] labelValues = metric.staticlabels.values().toArray(String[]::new);
            //List<Document> pipelineDocuments = createPipelineFromString(metric.query.trim());
            //AggregateIterable<Document> results = 
            //    collection.aggregate(pipelineDocuments)
            //        .allowDiskUse(false)
            //        .batchSize(1)
            //        .first();
            Stream<String> grpLabel = Stream.empty();
            if(metric.groupLabel != null) {
                grpLabel = metric.groupLabel.stream();
            }


            GaugeWithCallback.builder()
                    .name(metric.name)
                    .help(metric.help)
                    .labelNames(
                        Stream.concat(
                            metric.staticlabels.keySet().stream(),
                            grpLabel
                        ).toArray(String[]::new)
                    )
                    .callback(callback -> {
                        try{
                            logger.info("Inside Callback for Metric: " + metric.name);
                            logger.info("metric.query = " + metric.query);
                            logger.info("Config metric.query = " + metricCopy.query);
                            metric.query = metricCopy.query;
                            String[] currentquery = {"a"};
                            metric.placeholders.forEach((key, value) -> {
                                String TempValue = expressionEvaluator.evaluateExpression(logger,value);
                                logger.info("Mitric Key : " + key);
                                logger.info("Mitric Value : " + value);
                                logger.info("Mitric TempValue : " + TempValue);
                                logger.info("Mitric metric.query 1 : " + metric.query);
                                currentquery[0] = metric.query.replace("{{" + key + "}}", TempValue);
                                //metric.query = metric.query.replace("{{" + key + "}}", TempValue);
                                logger.info("Mitric metric.query 2 : " + currentquery[0]);
                            });
                            ArrayList<String> al = new ArrayList<String>();
                            List<Document> pipeline =
                                BsonArray.parse(currentquery[0])
                                .stream()
                                .map(v -> new Document(v.asDocument()))
                                .collect(Collectors.toList());
                            for (Document result : collection.aggregate(pipeline)) {
                                Object valueObj = result.get(metric.field);
                                logger.info("Result : " + result.toString());
                                Object groupObj = result.get("_id");
                                logger.info("Inside Callback for Metric value: " + valueObj.toString());
                                if (!(valueObj instanceof Number)) {
                                    logger.info("Inside If to Continue");
                                    continue;
                                }
                                if(groupObj != null && (groupObj instanceof Document)){
                                    logger.info("Inside If to Group");
                                    Document idDoc = (Document) groupObj;
                                    List<String> v_labelValues = new ArrayList<>();
                                    metric.staticlabels.values().forEach(v -> v_labelValues.add(v));
                                    for (String label : metric.groupLabel) {
                                        Object v = idDoc.get(label);
                                        v_labelValues.add(v != null ? v.toString() : "unknown");
                                    }
                                    callback.call(
                                        ((Number) valueObj).doubleValue(),
                                        v_labelValues.toArray(new String[v_labelValues.size()])
                                    );
                                }
                                else{
                                    logger.info("Inside If to No Group");
                                    List<String> v_labelValues = new ArrayList<>();
                                    metric.staticlabels.values().forEach(v -> v_labelValues.add(v));
                                    callback.call(
                                        ((Number) valueObj).doubleValue(),
                                        v_labelValues.toArray(new String[v_labelValues.size()])
                                    );
                                }
                            }                            
                        }catch (Exception e) {
                            // NEVER fail scrape
                            logger.info(e.toString());
                            //callback.call(Double.NaN, labelValues);
                        }
                   })
                    .register();
        }
    }
    public static List<Document> createPipelineFromString(String jsonPipelineString) {
        Gson gson = new Gson();
        // Define the type for Gson to deserialize into: a List of Maps
        Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
        
        List<Map<String, Object>> stagesList = gson.fromJson(jsonPipelineString, listType);
        List<Document> pipeline = new ArrayList<>();

        // Convert each stage (Map) into a MongoDB Document
        for (Map<String, Object> stage : stagesList) {
            pipeline.add(new Document(stage));
        }

        return pipeline;
    }
}
