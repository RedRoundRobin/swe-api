package com.redroundrobin.thirema.apirest.models;

public class Sensor {
    private int id;
    private long timestamp;
    private int value;

    public Sensor(int id, long timestamp, int value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }
}
