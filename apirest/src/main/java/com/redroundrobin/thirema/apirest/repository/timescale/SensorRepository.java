package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Timestamp> {
}
