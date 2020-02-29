package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Devices {
    public List<Device> devicesList;

    public Devices(List<Device> devicesList) {
        this.devicesList = devicesList;
    }
}
