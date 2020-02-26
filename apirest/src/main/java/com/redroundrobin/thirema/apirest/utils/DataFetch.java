package com.redroundrobin.thirema.apirest.utils;
import com.google.gson.JsonObject;


import java.util.List;

public class DataFetch {

    public static List<JsonObject> getForTopics(String [] topics) throws InterruptedException {

        Consumatore cons = new Consumatore(topics, "localhost:29092");
        List<JsonObject> mex = cons.fetchMessage();
        cons.chiudi();
        return mex;
    }
}
