package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Alert;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Timestamp> {
}
