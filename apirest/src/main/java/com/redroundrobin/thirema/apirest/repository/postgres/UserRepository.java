package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  User findByEmail(String email);

  User findByTelegramName(String telegramName);

  User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat);

  User findByuserId(int user_id);

  @Query("SELECT device_id " +
      "FROM users u, entities e, sensors s, devices d, alerts a" +
      "WHERE u.user_id IN : userId and u.entity_id = e.entity_id and e.entity_id = a.entity_id" +
      "and a.sensor_id = s.sensor_id and s.device_id = d.devide_id")
  List<Device> userDevices(@Param("userId") int userId);
}
