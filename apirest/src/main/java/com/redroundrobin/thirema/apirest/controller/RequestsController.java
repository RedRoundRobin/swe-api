package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.utils.DataFetch;
import com.redroundrobin.thirema.apirest.models.Topic;
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
    @RequestMapping(value = {"/devices"})
    public Devices devices() {
        Devices devs = new Devices();
        devs.setMessage(DataFetch.getDevices());
        return devs;
    }

    //Richiesta lista sensori ed info di un device sapendo l'id del device
    @RequestMapping(value = {"/device/{deviceid:.+}"})
    public Device device(@PathVariable("deviceid") String ID) {
        Device dev = new Device(ID);
        dev.setMessage(DataFetch.getDevice(ID));
        return dev;
    }

    //Richiesta informazioni sensore sapendo id del device ed id del sensore
    @RequestMapping(value = {"/sensor/{??????}"})
    public Sensor sensor(@PathVariable("") String ID) {
        Sensor s = new Sensor(IDDevice, IDSensor);
        s.setMessage(DataFetch.getSensor(IDDevice, IDSensor));
        return s;
    }


}
