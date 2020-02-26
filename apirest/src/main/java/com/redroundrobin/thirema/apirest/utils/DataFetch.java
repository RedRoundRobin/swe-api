package com.redroundrobin.thirema.apirest.utils;
import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.utils.Consumatore;

import static com.redroundrobin.thirema.apirest.utils.Consumatore.rispostaConsumatore;

import java.util.List;

public class DataFetch {

    public static List<JsonObject> getForTopic(String topic) throws InterruptedException {
        Consumatore cons = new Consumatore(topic, "localhost:29092");
        List<JsonObject> mex = rispostaConsumatore(cons);
        cons.chiudi();
        return mex;
    }
}
