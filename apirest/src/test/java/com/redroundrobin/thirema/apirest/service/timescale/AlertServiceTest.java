package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Alert;
import com.redroundrobin.thirema.apirest.repository.timescale.AlertRepository;
import com.redroundrobin.thirema.apirest.service.timescale.AlertService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AlertServiceTest {

  @MockBean
  private AlertRepository repo;

  private AlertService alertService;

  Alert alert1111;
  Alert alert1112;
  Alert alert1113;
  List<Alert> allG1D1S1Alerts;

  List<Alert> allAlerts;

  @Before
  public void setUp() {
    alertService = new AlertService(repo);

    alert1111 = new Alert();
    alert1111.setTime(new Timestamp(100));
    alert1111.setGatewayId(1);
    alert1111.setDeviceId(1);
    alert1111.setSensorId(1);
    alert1111.setValue(1);

    alert1112 = new Alert();
    alert1112.setTime(new Timestamp(200));
    alert1112.setGatewayId(1);
    alert1112.setDeviceId(1);
    alert1112.setSensorId(1);
    alert1112.setValue(2);

    alert1113 = new Alert();
    alert1113.setTime(new Timestamp(300));
    alert1113.setGatewayId(1);
    alert1113.setDeviceId(1);
    alert1113.setSensorId(1);
    alert1113.setValue(3);

    allAlerts = new ArrayList<>();
    allAlerts.add(alert1111);
    allAlerts.add(alert1112);
    allAlerts.add(alert1113);

    // all gateway 1 device 1 sensor 1 alerts
    allG1D1S1Alerts = new ArrayList<>();
    allG1D1S1Alerts.add(alert1111);
    allG1D1S1Alerts.add(alert1112);
    allG1D1S1Alerts.add(alert1113);


    when(repo.findAll()).thenReturn(allAlerts);
    when(repo.findAllByGatewayIdAndDeviceIdAndSensorId(anyInt(), anyInt(), anyInt()))
        .thenAnswer(i -> {
      int gatewayId = i.getArgument(0);
      int deviceId = i.getArgument(1);
      int sensorId = i.getArgument(2);
      return allAlerts.stream().filter(a -> a.getGatewayId() == gatewayId
          && a.getDeviceId() == deviceId && a.getSensorId() == sensorId)
          .collect(Collectors.toList());
    });
    when(repo.findTopNByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(anyInt(), anyInt(), anyInt(),
        anyInt())).thenAnswer(i -> {
      int resultsNumber = i.getArgument(0);
      int gatewayId = i.getArgument(1);
      int deviceId = i.getArgument(2);
      int sensorId = i.getArgument(3);
      return allAlerts.stream().filter(a -> a.getGatewayId() == gatewayId
          && a.getDeviceId() == deviceId && a.getSensorId() == sensorId)
          .sorted((t1,t2) -> Long.compare(t2.getTime().getTime(),t1.getTime().getTime()))
          .limit(resultsNumber).collect(Collectors.toList());
    });
    when(repo.findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(anyInt(), anyInt(),
        anyInt())).thenAnswer(i -> {
          int gatewayId = i.getArgument(0);
          int deviceId = i.getArgument(1);
          int sensorId = i.getArgument(2);
          return allAlerts.stream().filter(a -> a.getGatewayId() == gatewayId
              && a.getDeviceId() == deviceId && a.getSensorId() == sensorId)
              .sorted((t1,t2) -> Long.compare(t2.getTime().getTime(),t1.getTime().getTime()))
              .findFirst().orElse(null);
    });
  }

  @Test
  public void findAllSuccessfull() {
    List<Alert> alerts = alertService.findAll();

    assertEquals(allAlerts, alerts);
  }

  @Test
  public void findAllGateway1Device1Sensor1Alerts() {
    List<Alert> alerts = alertService.findAllByGatewayIdAndDeviceIdAndSensorId(1,1,1);

    assertEquals(allG1D1S1Alerts, alerts);
  }

  @Test
  public void findTop2ByGatewayIdAndDeviceIdAndSensorIdSuccessfull() {
    List<Alert> alerts = alertService.findTopNByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(2, 1,
        1, 1);

    assertTrue(alerts.size() == 2);
  }

  @Test
  public void findLastOneByGatewayIdAndDeviceIdAndSensorIdSuccessfull() {
    Alert alert = alertService.findLastOneByGatewayIdAndDeviceIdAndSensorId(1,1,1);

    assertEquals(alert1113, alert);
  }
}
