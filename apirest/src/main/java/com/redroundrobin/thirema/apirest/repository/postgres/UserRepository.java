package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  Iterable<User> findAllByEntity(Entity entity);

  Iterable<User> findAllByDisabledAlerts(Alert disabledAlert);

  Iterable<User> findAllByDisabledAlertsIn(Iterable<Alert> disabledAlerts);

  User findByEmail(String email);

  User findByTelegramName(String telegramName);

  User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat);

  @Transactional
  @Modifying
  @Query(value = "UPDATE User u SET u.deleted = true WHERE u.entity = :entity")
  void setDeletedByEntity(Entity entity);
}
