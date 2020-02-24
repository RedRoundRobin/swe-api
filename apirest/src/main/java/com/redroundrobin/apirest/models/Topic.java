package com.redroundrobin.apirest.models;

/*
   Questa classe va usata solamente per Debugging
   e Prima implementazione
 */

public class Topic {
    public final long id;
    public final String message = "Watch out for the corona virus. lmao";

    public Topic(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
