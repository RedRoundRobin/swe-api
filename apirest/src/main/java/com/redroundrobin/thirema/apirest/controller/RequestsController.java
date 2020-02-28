package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.utils.*;
import com.redroundrobin.thirema.apirest.models.Topic;
import com.redroundrobin.thirema.apirest.models.*;
import org.springframework.web.bind.annotation.*;


/*
    Il RequestsController possiamo provare a tenerlo unico, ma se serve
    suddividiamo in più controller indipendenti in base alla difficoltà.
    ---------------------------------------------------------
    Guida generale: https://spring.io/guides/gs/rest-service/
    Domande guida: https://stackoverflow.com/a/31422634
 */

@RestController
public class RequestsController {


    @RequestMapping(value = {"/topic/{topicid:.+}"})
    public Topic topic(@PathVariable("topicid") String ID) throws InterruptedException {
        Topic t = new Topic(ID);
        t.setMessage(DataFetch.getForTopics(new String[] {ID}));
        return t;
    }

    //Richiesta lista dispositivi
    //Le info richieste sono un array con gli ID del dispositivi
    @RequestMapping(value = {"/devices"})
    public Devices devices() {
        return DataFetch.getDevices();
    }

    //Richiesta lista sensori ed info di un device sapendo l'id del device
    //Le informazioni richieste sono ID del dispositivo, timestamp del dispositivo ed array con (ID sensore, timestamp e dato)
    @RequestMapping(value = {"/device/{deviceid:.+}"})
    public Device device(@PathVariable("deviceid") String ID) {
        return DataFetch.getDevice(ID);
    }

    //Richiesta informazioni sensore sapendo id del device ed id del sensore
    //Le informazioni richieste sono: ID del sensore, timestamp ed il dato
    @RequestMapping(value = {"/sensor/{deviceid:.+}/{sensorid:.+}"})
    public Sensor sensor(@PathVariable("deviceid") String IDDevice, @PathVariable("sensorid") String IDSensor) {
        return DataFetch.getSensor(IDDevice, IDSensor);
    }


}
