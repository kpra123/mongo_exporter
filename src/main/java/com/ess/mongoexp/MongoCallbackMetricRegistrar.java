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

public class MongoCallbackMetricRegistrar {
    //private final CollectorRegistry registry = new CollectorRegistry();
    //private final PushGateway pushGateway;
    public static void register(MongoDatabase database, AppConfig config){
        //MongoDatabase database, List<MetricConfig> metrics
        for (MetricConfig metric : config.metrics) {
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
                            ArrayList<String> al = new ArrayList<String>();
                            List<Document> pipeline =
                                BsonArray.parse(metric.query)
                                .stream()
                                .map(v -> new Document(v.asDocument()))
                                .collect(Collectors.toList());
                            for (Document result : collection.aggregate(pipeline)) {
                                Object valueObj = result.get(metric.field);
                                Object groupObj = result.get("_id");
                                if (!(valueObj instanceof Number)) {
                                    continue;
                                }
                                if(groupObj != null && (groupObj instanceof Document)){
                                    Document idDoc = (Document) groupObj;
                                    List<String> v_labelValues = new ArrayList<>();
                                    metric.staticlabels.values().forEach(v -> v_labelValues.add(v));
                                    for (String label : metric.groupLabel) {
                                        Object v = idDoc.get(label);
                                        v_labelValues.add(v != null ? v.toString() : "unknown");
                                    }
                                    callback.call(
                                        ((Number) valueObj).doubleValue(),
                                        v_labelValues.toArray(new String[al.size()])
                                    );
                                }
                                else{
                                    List<String> v_labelValues = new ArrayList<>();
                                    metric.staticlabels.values().forEach(v -> v_labelValues.add(v));
                                    callback.call(
                                        ((Number) valueObj).doubleValue(),
                                        v_labelValues.toArray(new String[al.size()])
                                    );
                                }
                            }                            
                        }catch (Exception e) {
                            // NEVER fail scrape
                            System.out.println(e.toString());
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
