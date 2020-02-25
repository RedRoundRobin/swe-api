package com.redroundrobin.apirest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.redroundrobin.apirest.Consumatore.rispostaConsumatore;
import com.redroundrobin.apirest.DataFilter;

import java.time.Duration;

public class DataFetch {

    public static JsonElement getForTopic(String topic) throws InterruptedException {
        Consumatore cons = new Consumatore(topic, "MyConsumer", "localhost:29092");
        DataFilter data = new DataFilter();
        data.setJsonData(rispostaConsumatore(cons));
        data.filterByTime(Duration.ofSeconds(5));
        return data.get();
    }
}
