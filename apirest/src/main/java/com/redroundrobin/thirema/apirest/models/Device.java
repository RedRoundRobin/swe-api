package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;

import java.util.List;

public class Device {
    public final String ID;
    public String message;

    public Device(String ID) { this.ID = ID;}

    public String getID() { return ID; }
    public String setMessage(JsonObject device) { message = device.toString(); return message; }
}
