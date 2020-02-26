package com.redroundrobin.thirema.apirest.utils;
import com.google.gson.JsonObject;

import static com.redroundrobin.thirema.apirest.utils.Consumatore.fetchMessage;

import java.util.List;

public class DataFetch {

    public static List<JsonObject> getForTopics(String [] topics) throws InterruptedException {

        Consumatore cons = new Consumatore(topics, "localhost:29092");
        List<JsonObject> mex = fetchMessage(cons);
        cons.chiudi();
        return mex;
    }
}
