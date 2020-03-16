package com.redroundrobin.thirema.apirest.models.postgres;

public class DeviceNotFoundException extends Exception {
    public DeviceNotFoundException(String message) {
        super(message);
    }
}
