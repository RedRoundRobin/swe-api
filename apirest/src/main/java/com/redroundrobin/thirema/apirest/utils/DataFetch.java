package com.redroundrobin.thirema.apirest.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataFetch {

    // final String[] topics = new String[] {"US-GATEWAY-1", "SG-GATEWAY-2", "DE-GATEWAY-3"};
    private final String[] topics;

    public DataFetch() {
        topics = new String[] {"US-GATEWAY-1", "SG-GATEWAY-2", "DE-GATEWAY-3"};
    }

    public List<JsonObject> getForTopics(String [] topics) {
        Consumer consumer = new Consumer(topics, "host.redroundrobin.site:29092");
        List<JsonObject> message = consumer.fetchMessage();
        consumer.close();
        return message;
    }
/*
    public Devices getDevices() {
        List<Device> devices = new ArrayList<>();

        for (JsonObject jsonDevice : getForTopics(this.topics)) {
            devices.add(createDeviceFromJsonObject(jsonDevice));
        }

        return new Devices(devices);
    }
*/
    public Device getDevice(int deviceId) throws DeviceNotFoundException {
        Optional<JsonObject> optionalJsonDevice = getForTopics(this.topics).stream().filter(jsonDevice -> jsonDevice.get("deviceId").getAsInt() == deviceId).findFirst();

        if (optionalJsonDevice.isEmpty()) {
            throw new DeviceNotFoundException("Device " + deviceId + " not found!");
        }

        return createDeviceFromJsonObject(optionalJsonDevice.get());
    }

    public Sensor getSensor(int deviceId, int sensorId) throws DeviceNotFoundException, SensorNotFoundException {
        Optional<JsonObject> optionalJsonDevice = getForTopics(this.topics).stream().filter(jsonDevice -> jsonDevice.get("deviceId").getAsInt() == deviceId).findFirst();

        if (optionalJsonDevice.isEmpty()) {
            throw new DeviceNotFoundException("Device " + deviceId + " not found!");
        }

        JsonObject sensor = null;
        for (JsonElement jsonSensor : optionalJsonDevice.get().getAsJsonArray("sensors")) {
            if (jsonSensor.getAsJsonObject().get("sensorId").getAsInt() == sensorId) {
                sensor = jsonSensor.getAsJsonObject();
                break;
            }
        }

        if (sensor == null) {
            throw new SensorNotFoundException("Device " + deviceId + ": sensor " + sensorId + " not found!");
        }

        return new Sensor();//sensor.get("sensorId").getAsInt(), sensor.get("timestamp").getAsLong(), sensor.get("data").getAsInt());
    }

    private Device createDeviceFromJsonObject(JsonObject jsonDevice) {
        List<Sensor> sensors = new ArrayList<>();

        for(JsonElement jsonSensor : jsonDevice.get("sensors").getAsJsonArray()) {
            JsonObject sensor = jsonSensor.getAsJsonObject();
            sensors.add(new Sensor());//sensor.get("sensorId").getAsInt(), sensor.get("timestamp").getAsLong(), sensor.get("data").getAsInt()));
        }

        return new Device();//jsonDevice.get("deviceId").getAsInt(), jsonDevice.get("timestamp").getAsLong(), sensors);
    }

    private static List<JsonObject> testMessage() {
        // TEST STRING GENERATOR
        String test = "[{\"deviceId\":1,\"timestamp\":1582818576871,\"sensors\":[{\"sensorId\":1,\"timestamp\":1582818576620,\"data\":5},{\"sensorId\":2,\"timestamp\":1582818576871,\"data\":5}]}, {\"deviceId\":2,\"timestamp\":1582818577121,\"sensors\":[{\"sensorId\":1,\"timestamp\":1582818577121,\"data\":9}]}]";
        List<JsonObject> message = new ArrayList<>();

        for (JsonElement jsonDevice : JsonParser.parseString(test).getAsJsonArray()) {
            JsonObject device = jsonDevice.getAsJsonObject();
            message.add(device);
        }

        return message; // Usare "message" come List<JsonObject> di output da un possibile consumatore.
    }
}
