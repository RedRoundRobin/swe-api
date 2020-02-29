package com.redroundrobin.thirema.apirest.models;

/*
   Questa classe va usata solamente per fare debugging e come prima implementazione
 */

import com.google.gson.JsonObject;

import java.util.List;

public class Topic {
    private String id;
    private String message;

    public Topic(String id) {
        this.id = id;
        this.message = "";
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(List<JsonObject> s) {
        message = s.toString();
    }
}
