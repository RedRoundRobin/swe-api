package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Logs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface LogsTRepository extends JpaRepository<Logs, Timestamp> {
}

