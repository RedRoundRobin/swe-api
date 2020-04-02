package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.repository.timescale.LogRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class LogServiceTest {

  private LogService logService;

  @MockBean
  private LogRepository logRepository;

  @Before
  public void setUp() {
    logService = new LogService(logRepository);

    when(logRepository.save(any(Log.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  public void createLog() {
    logService.createLog(1, "localhost", "operation", "data");

    assertTrue(true);
  }
}
