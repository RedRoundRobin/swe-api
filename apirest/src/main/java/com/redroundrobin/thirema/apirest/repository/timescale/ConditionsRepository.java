package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Conditions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionsRepository
        extends JpaRepository<Conditions, Long> { }
