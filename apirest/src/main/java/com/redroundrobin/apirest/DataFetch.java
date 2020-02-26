package com.redroundrobin.apirest;
import com.google.gson.JsonObject;
import static com.redroundrobin.apirest.Consumatore.rispostaConsumatore;

import java.util.List;

public class DataFetch {

    public static List<JsonObject> getForTopic(String topic) throws InterruptedException {
        Consumatore cons = new Consumatore(topic, "localhost:29092");
        List<JsonObject> mex = rispostaConsumatore(cons);
        cons.chiudi();
        return mex;
    }
}
