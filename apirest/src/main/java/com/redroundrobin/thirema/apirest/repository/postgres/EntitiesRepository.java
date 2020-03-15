package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Entities;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitiesRepository extends JpaRepository<Entities, Integer> { }
