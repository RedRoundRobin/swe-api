package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  User findByEmail(String email);

  User findByTelegramName(String telegramName);

  User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat);

  @Query("SELECT d.device_id " +
      "FROM User u,  Entity e, Sensor s,  Device d, Alert a " +
      "WHERE u.user_id = ?1 and u.entity = e.entityId and e.entityId = a.entity " +
      "and a.sensor = s.sensor_id and s.device = d.device_id")
  List<Device> userDevices(int userId);
}
