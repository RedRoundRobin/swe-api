package com.redroundrobin.thirema.apirest.models;

public class Sensor {
    private int sensorId;
    private long timestamp;
    private int value;

    public Sensor(int sensorId, long timestamp, int value) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getSensorId() {
        return sensorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }
}
