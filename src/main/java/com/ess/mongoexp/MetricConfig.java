package com.ess.mongoexp;

import java.util.List;
import java.util.Map;

public class MetricConfig {
    public String name;
    public String help;
    public String collection;
    public Map<String, String> placeholders;
    public String query;
    public String type;   // count | sum
    public String field;  // required for sum
    public List<String> groupLabel;
    public Map<String, String> staticlabels;
}
