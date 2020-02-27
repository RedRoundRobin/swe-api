package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Devices {
    public String message;

    public Devices(){};
    public String setMessage(List<JsonObject> devices) { message = devices.toString(); return message; }
}
