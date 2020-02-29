package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

import static org.apache.kafka.server.quota.ClientQuotaEntity.ConfigEntityType.CLIENT_ID;

public class Consumer {
    private String[] topics;
    private org.apache.kafka.clients.consumer.Consumer<Long, String> consumer;

    Consumer(String[] topics, String boostrapServers) {
        this.topics = topics;

        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers);
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, CLIENT_ID.toString());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        org.apache.kafka.clients.consumer.Consumer<Long, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topics));
        this.consumer = consumer;
    }

    public void close(){
        System.out.println("[Consumer] Closed");
        consumer.close();
    }

    public List<JsonObject> fetchMessage() {
        System.out.println("[Consumer] Started");

        List<JsonObject> devicesData = new ArrayList<>();

        final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds(3));

        if (consumerRecords.count() == 0) {
            System.out.println("[Consumer] Zero messages found");
            return devicesData;
        }

        Map<String, Boolean> checkTopic = new HashMap<>();
        for (String topic : topics) {
            checkTopic.put(topic, false);
        }

        for (ConsumerRecord<Long, String> record : consumerRecords) {
            if (System.currentTimeMillis() - record.key() <= 5000 && !checkTopic.get(record.topic())) {
                for (JsonElement data : JsonParser.parseString(record.value()).getAsJsonArray()) {
                    JsonObject deviceData = data.getAsJsonObject();
                    devicesData.add(deviceData);
                    checkTopic.replace(record.topic(), true);
                }
            }
        }

        return devicesData;
    }

    public static void main(String[] args) {
        Consumer test = new Consumer(new String[] {"Aiuto"} , "localhost:29092");
        test.fetchMessage();
    }
}
