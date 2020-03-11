package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.City;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends CrudRepository<City, Long> {

}