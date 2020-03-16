package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.Entity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends CrudRepository<Entity, Integer> {

}
