package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import java.util.List;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends CrudRepository<Entity, Integer> {
  Iterable<Entity> findAllBySensors(Sensor sensor);

  Iterable<Entity> findAllBySensorsAndUsers(Sensor sensor, User user);

  Iterable<Entity> findAllByUsers(User user);
}
