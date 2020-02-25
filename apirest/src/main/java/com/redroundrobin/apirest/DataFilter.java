package com.redroundrobin.apirest;

import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataFilter {

    List<JsonObject> jsonData = new ArrayList<JsonObject>();

    public DataFilter(){ }

    public void setJsonData(List<JsonObject> jd) {
        jsonData = jd;
    }

    public List<JsonObject> filterByTime(Duration delay) {

        long time = System.currentTimeMillis();

        List<JsonObject> filteredData = jsonData.stream()
                                                .filter(x -> x.get("timestamp").getAsLong() >= time - delay.toMillis())
                                                .collect(Collectors.toList());

        return filteredData;
    }

}
