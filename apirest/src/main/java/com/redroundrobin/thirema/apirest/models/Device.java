package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Device {
    public final String deviceId;
    public String message;

    public Device(String deviceId) { this.deviceId = deviceId;}

    public String getDeviceId() { return deviceId; }
    public String setMessage(List<JsonObject> device) { message = device.toString(); return message; }
}
