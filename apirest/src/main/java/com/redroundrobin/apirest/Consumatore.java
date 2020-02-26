package com.redroundrobin.apirest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.text.SimpleDateFormat;
import java.util.*;
import java.time.Duration;
import com.google.gson.JsonObject;

import static org.apache.kafka.server.quota.ClientQuotaEntity.ConfigEntityType.CLIENT_ID;

public class Consumatore {
    private String topic;
    private String bootstrapServers; //Lista di indirizzoIP:porta separati da una virgola.
    private Consumer<Long, String> consumatore;

    //definisco la classe consumatore e ne istanzio uno
    Consumatore(String topic, String boostrapServers) {
        this.topic = topic;
        this.bootstrapServers = boostrapServers;

        //Imposto le proprietà del consumatore da creare
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CLIENT_ID.toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());


        Consumer<Long, String> cons = new KafkaConsumer<>(props);

        //Sottoscrivo il consumatore al topic
        cons.subscribe(Collections.singletonList(topic));

        this.consumatore = cons;
    }

    public void chiudi(){
        consumatore.close();
    }

    //mi collego a kafka e prendo i records del consumatore
    public static List<JsonObject> rispostaConsumatore(Consumatore consumatore) throws InterruptedException {
        System.out.println("Consumatore richiesta dati avviata");

        List<JsonObject> datiDispositivi = new ArrayList<JsonObject>();

        final ConsumerRecords<Long, String> recordConsumatore = consumatore.consumatore.poll(Duration.ofSeconds(3));

        if (recordConsumatore.count()==0) {
            System.out.println("Nessun record trovato");
        }

        long tempoRichiesta;
        for(ConsumerRecord<Long, String> record : recordConsumatore) {
            tempoRichiesta = System.currentTimeMillis();

            //Per ogni record trovato controllo se il timestamp della chiave è più recente di 5 secondi
            if(tempoRichiesta - record.key() <= 5000){
                System.out.println("Trovato");
                JsonArray dati = new JsonParser().parseString(record.value()).getAsJsonArray();
                for (JsonElement elemento: dati) {
                    JsonObject datiDispositivo = elemento.getAsJsonObject();
                    System.out.println(datiDispositivo);
                    System.out.println();
                    datiDispositivi.add(datiDispositivo);
                }
            }

        };


        System.out.println("Messaggi disponibili consumati!");
        //System.out.println(datiDispositivi);
        return datiDispositivi;
    }

    public static void main(String args[]) throws Exception {

        Consumatore test = new Consumatore("Aiuto", "localhost:29092");

        Consumatore.rispostaConsumatore(test);
    }
}
