package com.redroundrobin.thirema.apirest.models;

import java.util.List;

public class Device {
    private int deviceId;
    private long timestamp;
    private List<Sensor> sensorsList;
    private int sensorsNumber;

    public Device(int deviceId, long timestamp, List<Sensor> sensorsList) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.sensorsList = sensorsList;
        this.sensorsNumber = sensorsList.size();
    }

    public int getDeviceId() {
        return deviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Sensor> getSensorsList() {
        return sensorsList;
    }

    public int getSensorsNumber() {
        return sensorsNumber;
    }
}
