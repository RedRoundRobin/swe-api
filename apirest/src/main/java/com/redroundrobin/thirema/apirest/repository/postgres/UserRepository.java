package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
  User findByEmail(String email);

  User findByTelegramName(String telegramName);

  User findByTelegramNameAndTelegramChat(String telegramName, String telegramChat);
}
