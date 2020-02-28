package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;


public class Device {
    public int deviceId;
    public long timestamp;
    public List<Sensor> sensorsList;
    public int sensorsNumber;

    public Device(int deviceId, long timestamp, List<Sensor> sensorsList) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.sensorsList = sensorsList;
        this.sensorsNumber = sensorsList.size();
    }

    public int getDeviceId() {
        return deviceId;
    }

}
