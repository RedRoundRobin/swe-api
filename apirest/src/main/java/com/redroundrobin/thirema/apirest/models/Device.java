package com.redroundrobin.thirema.apirest.models;

import java.util.List;

public class Device {
    private int id;
    private long timestamp;
    private List<Sensor> sensors;
    private int sensorsNumber;

    public Device(int id, long timestamp, List<Sensor> sensors) {
        this.id = id;
        this.timestamp = timestamp;
        this.sensors = sensors;
        this.sensorsNumber = sensors.size();
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public int getSensorsNumber() {
        return sensorsNumber;
    }
}
