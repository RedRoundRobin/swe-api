package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.service.postgres.AlertService;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.ViewGraphService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SensorServiceTest {

  @MockBean
  private SensorRepository repo;

  @MockBean
  private AlertService alertService;

  @MockBean
  private DeviceService deviceService;

  @MockBean
  private EntityService entityService;

  @MockBean
  private ViewGraphService viewGraphService;


  private SensorService sensorService;


  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  private Device device1;
  private Device device2;

  private Alert alert1;
  private Alert alert2;
  private Alert alert3;

  private ViewGraph viewGraph1;
  private ViewGraph viewGraph2;


  @Before
  public void setUp() {
    sensorService = new SensorService(repo);
    sensorService.setAlertService(alertService);
    sensorService.setDeviceService(deviceService);
    sensorService.setEntityService(entityService);
    sensorService.setViewGraphService(viewGraphService);

    entity1 = new Entity();
    entity1.setId(1);
    entity1.setName("entity1");

    entity2 = new Entity();
    entity2.setId(2);
    entity2.setName("entity2");

    entity3 = new Entity();
    entity3.setId(3);
    entity3.setName("entity3");

    List<Entity> allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);


    List<Entity> entities1 = new ArrayList<>();
    entities1.add(entity1);
    entities1.add(entity2);
    List<Entity> entities2 = new ArrayList<>();
    entities2.add(entity3);

    sensor1 = new Sensor();
    sensor1.setId(1);
    sensor1.setEntities(entities1);

    sensor2 = new Sensor();
    sensor2.setId(2);
    sensor2.setEntities(entities1);

    sensor3 = new Sensor();
    sensor3.setId(3);
    sensor3.setEntities(entities2);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    device1 = new Device();
    device1.setId(1);
    device1.setSensors(allSensors.stream().filter(s -> s.getId() <= 2)
        .collect(Collectors.toList()));

    device2 = new Device();
    device2.setId(2);
    device2.setSensors(allSensors.stream().filter(s -> s.getId() > 2)
        .collect(Collectors.toList()));

    List<Device> devices = new ArrayList<>();
    devices.add(device1);
    devices.add(device2);

    sensor1.setDevice(device1);
    sensor2.setDevice(device1);
    sensor3.setDevice(device2);

    alert1 = new Alert();
    alert1.setAlertId(1);
    alert1.setSensor(sensor1);

    alert2 = new Alert();
    alert2.setAlertId(2);
    alert2.setSensor(sensor1);

    alert3 = new Alert();
    alert3.setAlertId(3);
    alert3.setSensor(sensor2);

    List<Alert> alertsSensor1 = new ArrayList<>();
    alertsSensor1.add(alert1);
    alertsSensor1.add(alert2);
    sensor1.setAlerts(alertsSensor1);

    List<Alert> alertsSensor2 = new ArrayList<>();
    alertsSensor2.add(alert3);
    sensor2.setAlerts(alertsSensor2);

    sensor3.setAlerts(Collections.emptyList());

    List<Alert> allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);

    viewGraph1 = new ViewGraph();
    viewGraph1.setId(1);
    viewGraph1.setSensor1(sensor1);
    viewGraph1.setSensor2(sensor2);

    viewGraph2 = new ViewGraph();
    viewGraph2.setId(2);
    viewGraph2.setSensor1(sensor3);
    viewGraph2.setSensor2(sensor1);

    List<ViewGraph> viewGraphs1Sensor1 = new ArrayList<>();
    viewGraphs1Sensor1.add(viewGraph1);
    List<ViewGraph> viewGraphs1Sensor2 = new ArrayList<>();
    viewGraphs1Sensor2.add(viewGraph1);
    List<ViewGraph> viewGraphs2Sensor1 = new ArrayList<>();
    viewGraphs2Sensor1.add(viewGraph2);
    List<ViewGraph> viewGraphs2Sensor3 = new ArrayList<>();
    viewGraphs2Sensor3.add(viewGraph2);

    sensor1.setViewGraphs1(viewGraphs1Sensor1);
    sensor1.setViewGraphs2(viewGraphs2Sensor1);

    sensor2.setViewGraphs1(Collections.emptyList());
    sensor2.setViewGraphs2(viewGraphs1Sensor2);

    sensor3.setViewGraphs1(viewGraphs2Sensor3);
    sensor3.setViewGraphs2(Collections.emptyList());

    List<ViewGraph> allViewGraphs = new ArrayList<>();
    allViewGraphs.add(viewGraph1);
    allViewGraphs.add(viewGraph2);

    when(repo.findAll()).thenReturn(allSensors);
    when(repo.findAllByDevice(any(Device.class))).thenAnswer(i -> {
      Device device = devices.stream().filter(d -> i.getArgument(0).equals(d))
          .findFirst().orElse(null);
      return allSensors.stream().filter(s -> s.getDevice().equals(device))
          .collect(Collectors.toList());
    });
    when(repo.findAllByEntities(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allSensors.stream().filter(s -> s.getEntities().contains(entity))
          .collect(Collectors.toList());
    });
    when(repo.findAllByViewGraphs1OrViewGraphs2(any(ViewGraph.class),any(ViewGraph.class))).thenAnswer(i -> {
      return allSensors.stream().filter(s -> s.getViewGraphs1().contains(
          i.getArgument(0)) || s.getViewGraphs2().contains(i.getArgument(1)))
          .collect(Collectors.toList());
    });
    when(repo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });
    when(repo.findByAlerts(any(Alert.class))).thenAnswer(i -> {
      Alert alert = i.getArgument(0);
      return allSensors.stream().filter(s -> s.getAlerts().contains(alert))
          .findFirst().orElse(null);
    });

    when(alertService.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getAlertId()))
          .findFirst().orElse(null);
    });

    when(deviceService.findById(anyInt())).thenAnswer(i -> {
      return devices.stream().filter(d -> i.getArgument(0).equals(d.getId()))
          .findFirst().orElse(null);
    });

    when(entityService.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst().orElse(null);
    });

    when(viewGraphService.findById(anyInt())).thenAnswer(i -> {
      return allViewGraphs.stream().filter(vg -> i.getArgument(0).equals(vg.getId()))
          .findFirst().orElse(null);
    });
  }

  @Test
  public void findAllSensors() {
    List<Sensor> sensors = sensorService.findAll();

    assertTrue(!sensors.isEmpty());
  }

  @Test
  public void findAllSensorsByDeviceId() {
    List<Sensor> sensors = sensorService.findAllByDeviceId(device1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByDeviceEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByDeviceId(4);

    assertTrue(sensors.stream().count() == 0);
  }

  @Test
  public void findAllSensorsByEntityId() {
    List<Sensor> sensors = sensorService.findAllByEntityId(entity1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByEntitesEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByEntityId(4);

    assertTrue(sensors.stream().count() == 0);
  }

  @Test
  public void findAllSensorsByViewGraphId() {
    List<Sensor> sensors = sensorService.findAllByViewGraphId(viewGraph1.getId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByViewGraphEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByViewGraphId(4);

    assertTrue(sensors.stream().count() == 0);
  }

  @Test
  public void findSensorById() {
    Sensor sensor = sensorService.findById(sensor1.getId());

    assertNotNull(sensor);
  }

  @Test
  public void findSensorByAlertId() {
    Sensor sensor = sensorService.findByAlertId(alert1.getAlertId());

    assertNotNull(sensor);
  }

  @Test
  public void findSensorByNotExistentAlertId() {
    Sensor sensor = sensorService.findByAlertId(4);

    assertNull(sensor);
  }
}
