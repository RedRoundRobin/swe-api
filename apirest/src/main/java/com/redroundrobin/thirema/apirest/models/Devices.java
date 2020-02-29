package com.redroundrobin.thirema.apirest.models;

import java.util.List;

public class Devices {
    private List<Device> devicesList;

    public Devices(List<Device> devicesList) {
        this.devicesList = devicesList;
    }

    public List<Device> getDevicesList() {
        return devicesList;
    }
}
