package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.repository.timescale.LogRepository;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

  private LogRepository repo;

  @Autowired
  public LogService(LogRepository repo) {
    this.repo = repo;
  }

  public void createLog(int userId, String ip, String operation, String data) {
    Log newLog = new Log();
    newLog.setTime(Timestamp.from(Instant.now()));
    newLog.setUserId(userId);
    newLog.setIp(ip);
    newLog.setOperation(operation);
    newLog.setData(data);

    repo.save(newLog);
  }
}
