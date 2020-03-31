package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewGraphRepository extends CrudRepository<ViewGraph, Integer> {

  Iterable<ViewGraph> findAllBySensor1OrSensor2(Sensor sensor1, Sensor sensor2);

  Iterable<ViewGraph> findAllByView(View view);
}
