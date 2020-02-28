package com.redroundrobin.thirema.apirest.models;

import java.util.List;

public class Devices {
    private List<Device> devices;

    public Devices(List<Device> devices) {
        this.devices = devices;
    }

    public List<Device> getDevices() {
        return devices;
    }
}
