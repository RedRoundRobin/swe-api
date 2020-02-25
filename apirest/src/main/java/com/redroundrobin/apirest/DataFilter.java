package com.redroundrobin.apirest;

import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataFilter {

    List<JsonObject> jsonData = new ArrayList<JsonObject>();


    public void setJsonData(List<JsonObject> jd) {
        jsonData = jd;
    }

    public void filterByTime(Duration delay) {

        long time = System.currentTimeMillis();

        System.out.println("- Before size: " + jsonData.size());

        List<JsonObject> filteredData = jsonData.stream()
                                                .filter(x -> x.get("timestamp").getAsLong() >= time - delay.toMillis())
                                                .collect(Collectors.toList());
        jsonData = filteredData;
        System.out.println("- After size: " + jsonData.size());
    }

    public JsonObject get() {
        return jsonData.size() > 0 ? jsonData.get(0) : new JsonObject();
    }

}
