package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;


public class Device {
    public String deviceId;
    public long timestamp;
    public List<Sensor> sensorsList;
    public int sensorsNumber;

    public Device(String deviceId, long timestamp, List<Sensor> sensorsList) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.sensorsList = sensorsList;
        this.sensorsNumber = sensorsList.size();
    }

    public String getDeviceId() {
        return deviceId;
    }

}
