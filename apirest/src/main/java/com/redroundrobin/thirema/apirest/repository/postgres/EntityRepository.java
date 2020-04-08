package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends CrudRepository<Entity, Integer> {
  Iterable<Entity> findAllBySensors(Sensor sensor);

  @Query("SELECT E FROM User U JOIN U.entity E JOIN E.sensors S WHERE S = :sensor AND U = :user")
  Iterable<Entity> findAllBySensorsAndUsers(Sensor sensor, User user);

  @Query("SELECT E FROM User U JOIN U.entity E WHERE U = :user")
  Iterable<Entity> findAllByUsers(User user);
}
