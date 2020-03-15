package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.*;
import com.redroundrobin.thirema.apirest.utils.DataFetch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/*
    Il RequestsController per ora si tiene unico, ma se serve si può suddividerlo in più controller indipendenti.
    ---------------------------------------------------------
    Guida generale: https://spring.io/guides/gs/rest-service/
    Domande guida: https://stackoverflow.com/a/31422634
 */

@RestController
public class RequestsController {

    @RequestMapping(value = {"/topic/{topicid:.+}"})
    public Topic topic(@PathVariable("topicid") String id) {
        Topic topic = new Topic(id);
        DataFetch dataFetch = new DataFetch();
        topic.setMessage(dataFetch.getForTopics(new String[] {id}));
        return topic;
    }

    // Richiesta lista dispositivi
    // Le informazioni richieste sono un array con gli id del dispositivi
    @RequestMapping(value = {"/devices"})
    public Devices devices() {
        DataFetch dataFetch = new DataFetch();
        return dataFetch.getDevices();
    }

    // Richiesta lista sensori ed informazioni di un device sapendo l'id del device
    // Le informazioni richieste sono id del dispositivo, timestamp del dispositivo ed array con (id sensore, timestamp e dato)
    @RequestMapping(value = {"/device/{deviceid:.+}"})
    public Device device(@PathVariable("deviceid") int ID) throws DeviceNotFoundException {
        DataFetch dataFetch = new DataFetch();
        return dataFetch.getDevice(ID);
    }

    // Richiesta informazioni sensore sapendo id del device ed id del sensore
    // Le informazioni richieste sono: ID del sensore, timestamp ed il dato
    @RequestMapping(value = {"/sensor/{deviceid:.+}/{sensorid:.+}"})
    public Sensor sensor(@PathVariable("deviceid") int idDevice, @PathVariable("sensorid") int idSensor) throws DeviceNotFoundException, SensorNotFoundException {
        DataFetch dataFetch = new DataFetch();
        return dataFetch.getSensor(idDevice, idSensor);
    }
}
