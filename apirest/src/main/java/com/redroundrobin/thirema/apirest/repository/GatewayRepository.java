package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.Gateway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRepository extends CrudRepository<Gateway, Integer> {

}