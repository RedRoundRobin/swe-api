package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;
import java.time.Duration;
import com.google.gson.JsonObject;

import static org.apache.kafka.server.quota.ClientQuotaEntity.ConfigEntityType.CLIENT_ID;

public class Consumatore {
    private String[] topics;
    private String bootstrapServers;
    private Consumer<Long, String> consumatore;


    Consumatore(String [] topics, String boostrapServers) {
        this.topics = topics;
        this.bootstrapServers = boostrapServers;


        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CLIENT_ID.toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        Consumer<Long, String> cons = new KafkaConsumer<>(props);

        cons.subscribe(Arrays.asList(topics));

        this.consumatore = cons;
    }

    public void chiudi(){
        System.out.println("[Consumatore] Chiuso");
        consumatore.close();
    }


    public List<JsonObject> fetchMessage() throws InterruptedException {
        System.out.println("[Consumatore] Avvio");

        List<JsonObject> datiDispositivi = new ArrayList<JsonObject>();

        final ConsumerRecords<Long, String> consumerRecords = consumatore.poll(Duration.ofSeconds(3));

        if (consumerRecords.count()==0) {
            System.out.println("[Consumatore] Nessun messaggio trovato");
            return datiDispositivi;
        }

        Map<String, Boolean> checkTopic = new HashMap<>();
        for(String topic : topics)
        {
            checkTopic.put(topic, false);
        }

        for(ConsumerRecord<Long, String> record : consumerRecords) {

            if(System.currentTimeMillis() - record.key() <= 5000 && checkTopic.get(record.topic()) == false) {
                JsonArray dati = new JsonParser().parseString(record.value()).getAsJsonArray();
                for (JsonElement elemento : dati) {
                    JsonObject datiDispositivo = elemento.getAsJsonObject();
                    datiDispositivi.add(datiDispositivo);
                    checkTopic.replace(record.topic(), true);
                }
            }
        }

        return datiDispositivi;
    }

    public static void main(String args[]) throws Exception {

        Consumatore test = new Consumatore(new String[] {"Aiuto"} , "localhost:29092");

<<<<<<< HEAD
        test.fetchMessage();
=======
        test.fetchMessage(test);
>>>>>>> springApi
    }
}
