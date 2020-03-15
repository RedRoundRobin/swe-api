package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface AlertsTRepository extends JpaRepository<Alerts, Timestamp> { }
