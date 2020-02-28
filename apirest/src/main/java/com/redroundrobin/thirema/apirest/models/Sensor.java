package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Sensor {
    public final String deviceId;
    public final String sensorId;
    public String message;

    public Sensor(String deviceId, String sensorId) { this.deviceId = deviceId; this.sensorId = sensorId; }

    public String getDeviceId() { return deviceId; }
    public String getSensorId() { return sensorId; }
    public String setMessage(List<JsonObject> sensor) { message = sensor.toString(); return message; }
}
