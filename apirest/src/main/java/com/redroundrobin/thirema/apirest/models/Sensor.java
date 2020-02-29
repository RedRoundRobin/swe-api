package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Sensor {
    public int sensorId;
    public long timestamp;
    public int value;

    public Sensor(int sensorId, long timestamp, int value) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.value = value;
    }
}
