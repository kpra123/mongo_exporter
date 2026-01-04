package com.ess.mongoexp;

import java.util.List;

public class AppConfig {
    public MongoConfig mongodb;
    public PrometheusConfig prometheus;
    public List<MetricConfig> metrics;
}
