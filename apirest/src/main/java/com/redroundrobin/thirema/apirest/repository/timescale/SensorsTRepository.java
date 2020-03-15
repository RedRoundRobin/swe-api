package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Sensors;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface SensorsTRepository extends JpaRepository<Sensors, Timestamp> { }
