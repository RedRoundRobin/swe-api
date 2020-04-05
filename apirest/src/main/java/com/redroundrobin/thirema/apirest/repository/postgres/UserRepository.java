package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  Iterable<User> findAllByEntity(Entity entity);

  Iterable<User> findAllByDisabledAlerts(Alert disabledAlert);

  Iterable<User> findAllByDisabledAlertsIn(Iterable<Alert> disabledAlerts);

  User findByEmail(String email);

  User findByTelegramName(String telegramName);

  User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat);

}
