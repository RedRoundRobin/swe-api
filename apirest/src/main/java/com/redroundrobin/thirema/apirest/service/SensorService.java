package com.redroundrobin.thirema.apirest.service;

import java.util.List;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    @Autowired
    private SensorRepository repository;

    public List<Sensor> findAll() {
        return (List<Sensor>) repository.findAll();
    }
    public Sensor find(int sensorId){
        return repository.findById(sensorId).get();
    }
}