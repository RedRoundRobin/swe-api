package com.redroundrobin.apirest.models;

/*
   Questa classe va usata solamente per Debugging
   e Prima implementazione
 */

import com.google.gson.JsonObject;

import java.util.List;

public class Topic {
    public final String id;
    public String message;

    public Topic(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setMessage(List<JsonObject> s) { message = s.toString(); }
}
