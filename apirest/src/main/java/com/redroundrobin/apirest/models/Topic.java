package com.redroundrobin.apirest.models;

/*
   Questa classe va usata solamente per Debugging
   e Prima implementazione
 */

import com.google.gson.JsonObject;

public class Topic {
    public final String id;
    public JsonObject message;

    public Topic(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setMessage(JsonObject s) { message = s; }
}
