package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.Sensor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends CrudRepository<Sensor, Integer> {

}