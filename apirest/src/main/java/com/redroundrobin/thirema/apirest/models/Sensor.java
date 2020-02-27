package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

public class Sensor {
    public final String deviceID;
    public final String sensorID;
    public String message;

    public Sensor(String deviceID, String sensorID) { this.deviceID = deviceID; this.sensorID = sensorID; }

    public String getDeviceID() { return deviceID; }
    public String getSensorID() { return sensorID; }
    public String setMessage(JsonObject sensor) { message = sensor.toString(); return message; }
}
