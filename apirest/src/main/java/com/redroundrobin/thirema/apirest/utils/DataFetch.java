package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DataFetch {

    // final String[] topics = new String[] {"US-GATEWAY-1", "SG-GATEWAY-2", "DE-GATEWAY-3"};
    private final String[] topics;

    public DataFetch() {
        topics = new String[] {"US-GATEWAY-1", "SG-GATEWAY-2", "DE-GATEWAY-3"};
    }

    public List<JsonObject> getForTopics(String [] topics) throws InterruptedException {
        Consumatore cons = new Consumatore(topics, "host.redroundrobin.site:29092");
        List<JsonObject> mex = cons.fetchMessage();
        cons.chiudi();
        return mex;
    }

    public Devices getDevices() throws InterruptedException {
        List<JsonObject> all = getForTopics(this.topics);
        List<Device> devices = new ArrayList<Device>();

        for(JsonObject jo : all)
        {
            devices.add(createDeviceFromJsonObject(jo));
        }
        return new Devices(devices);
    }

    public Device getDevice(int deviceId) throws InterruptedException {
        List<JsonObject> mex = getForTopics(this.topics);
        Optional<JsonObject> deviceObj;
        deviceObj = mex.stream().filter(jo -> jo.get("deviceId").getAsInt() == deviceId).findFirst();

        // Aggiungere isPresent() exception

        return createDeviceFromJsonObject(deviceObj.get());
    }

    public Sensor getSensor(int deviceId, int sensorId) throws InterruptedException {
        List<JsonObject> all = getForTopics(this.topics);

        Optional<JsonObject> device = all.stream()
                .filter(jsonObject -> jsonObject.get("deviceId").getAsInt() == deviceId)
                .findFirst();

        // Aggiungere isPresent() exception

        JsonArray sensors = device.get().getAsJsonArray("sensors");
        JsonObject sensorObj = null;
        for(JsonElement jo : sensors)
        {
            if(jo.getAsJsonObject().get("sensorId").getAsInt() == sensorId)
            {
                sensorObj = jo.getAsJsonObject();
                break;
            }
        }

        // Aggiungere null exception

        int senId = sensorObj.get("sensorId").getAsInt();
        long timestamp = sensorObj.get("timestamp").getAsLong();
        int value = sensorObj.get("data").getAsInt();

        return new Sensor(senId, timestamp, value);
    }

    protected Device createDeviceFromJsonObject(JsonObject jo)
    {
        List<Sensor> sensors = new ArrayList<Sensor>();
        JsonArray sensorsArray = jo.get("sensors").getAsJsonArray();

        for(JsonElement je : sensorsArray)
        {
            JsonObject joo = je.getAsJsonObject();
            sensors.add(new Sensor(joo.get("sensorId").getAsInt(), joo.get("timestamp").getAsLong(), joo.get("data").getAsInt()));
        }

        return new Device(jo.get("deviceId").getAsInt(), jo.get("timestamp").getAsLong(), sensors);
    }


    protected static List<JsonObject> testMessage() {
        // TEST STRING GENERATOR
        // ============================
        String mytest = "[{\"deviceId\":1,\"timestamp\":1582818576871,\"sensors\":[{\"sensorId\":1,\"timestamp\":1582818576620,\"data\":5},{\"sensorId\":2,\"timestamp\":1582818576871,\"data\":5}]}, {\"deviceId\":2,\"timestamp\":1582818577121,\"sensors\":[{\"sensorId\":1,\"timestamp\":1582818577121,\"data\":9}]}]";
        ;
        List<JsonObject> mex = new ArrayList<JsonObject>();
        JsonArray dati = new JsonParser().parseString(mytest).getAsJsonArray();
        for (JsonElement elemento : dati) {
            JsonObject datiDispositivo = elemento.getAsJsonObject();
            mex.add(datiDispositivo);
        }
        return mex;
        // Usare "mex" come List<JsonObject> di output da un possibile consumatore.
        // ============================
    }
}
