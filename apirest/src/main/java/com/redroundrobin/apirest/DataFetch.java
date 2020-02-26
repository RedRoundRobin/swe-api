package com.redroundrobin.apirest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.redroundrobin.apirest.Consumatore.rispostaConsumatore;
import com.redroundrobin.apirest.DataFilter;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class DataFetch {

    public static List<JsonObject> getForTopic(String topic) throws InterruptedException {
        Consumatore cons = new Consumatore(topic, "localhost:29092");
        List<JsonObject> lista = rispostaConsumatore(cons);
        System.out.println(lista);
        return lista;

    }
}
