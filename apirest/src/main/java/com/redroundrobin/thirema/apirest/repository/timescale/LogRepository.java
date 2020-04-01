package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Log;
import java.sql.Timestamp;
import org.springframework.data.repository.CrudRepository;

public interface LogRepository extends CrudRepository<Log, Timestamp> {
}

