package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Sensor {
    public String sensorId;
    public long timestamp;
    public int value;

    public Sensor(String sensorId, long timestamp, int value) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.value = value;
    }
}
