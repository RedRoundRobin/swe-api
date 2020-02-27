package com.redroundrobin.thirema.apirest.utils;
import com.google.gson.JsonObject;


import java.util.List;

public class DataFetch {

    final String[] topics = new String[] {"US-GATEWAY-1", "SG-GATEWAY-2", "DE-GATEWAY-3"};

    public static List<JsonObject> getForTopics(String [] topics) throws InterruptedException {
        Consumatore cons = new Consumatore(topics, "localhost:29092");
        List<JsonObject> mex = cons.fetchMessage();
        cons.chiudi();
        return mex;
    }
    public static List<JsonObject> getDevices(){
        return null;
    }
    public static JsonObject getDevice(String deviceId){

    }
//    JsonObject getSensor(String deviceId, String sensorId)


}
