package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

}
