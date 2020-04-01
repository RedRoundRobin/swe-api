package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Log;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Timestamp> {
}

