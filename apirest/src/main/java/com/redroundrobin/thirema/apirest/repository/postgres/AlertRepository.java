package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends CrudRepository<Alert, Integer> {
  Iterable<Alert> findAllByDeletedFalse();

  Iterable<Alert> findAllByEntityAndDeletedFalse(Entity entity);

  Iterable<Alert> findAllByEntityAndSensorAndDeletedFalse(Entity entity, Sensor sensor);

  Iterable<Alert> findAllBySensorAndDeletedFalse(Sensor sensor);

  Iterable<Alert> findAllByUsersAndDeletedFalse(User user);
}
