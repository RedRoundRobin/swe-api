package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findByEmail(String email);
    @Query("SELECT U.telegramName from User U")
    List<User> findByTelegramName(String telegramName);
    @Query("SELECT U.telegramChat from User U")
    List<User> findByTelegramChat(String telegramChat);
}
