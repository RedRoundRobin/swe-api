package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SensorServiceTest {

  @MockBean
  private SensorRepository repo;

  @MockBean
  private EntityService entityService;

  private SensorService sensorService;


  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;


  @Before
  public void setUp() {
    sensorService = new SensorService(repo);
    sensorService.setEntityService(entityService);

    entity1 = new Entity();
    entity1.setEntityId(1);
    entity1.setName("entity1");

    entity2 = new Entity();
    entity2.setEntityId(2);
    entity2.setName("entity2");

    entity3 = new Entity();
    entity3.setEntityId(3);
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
    sensor1.setSensorId(1);
    sensor1.setEntities(entities1);

    sensor2 = new Sensor();
    sensor2.setSensorId(2);
    sensor2.setEntities(entities1);

    sensor3 = new Sensor();
    sensor3.setSensorId(3);
    sensor3.setEntities(entities2);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);

    when(repo.findAll()).thenReturn(allSensors);
    when(repo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getSensorId()))
          .findFirst();
    });
    when(entityService.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getEntityId()))
          .findFirst().orElse(null);
    });
    when(repo.findAllByEntities(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allSensors.stream().filter(s -> s.getEntities().contains(entity))
          .collect(Collectors.toList());
    });
  }

  @Test
  public void findAllSensors() {
    List<Sensor> sensors = sensorService.findAll();

    assertTrue(!sensors.isEmpty());
  }

  @Test
  public void findAllSensorsByEntityId() {
    List<Sensor> sensors = sensorService.findAllByEntityId(entity1.getEntityId());

    assertTrue(sensors.stream().count() == 2);
  }

  @Test
  public void findAllSensorsByEntitesEmptyResult() {
    List<Sensor> sensors = sensorService.findAllByEntityId(4);

    assertTrue(sensors.stream().count() == 0);
  }

  @Test
  public void findSensorById() {
    Sensor sensor = repo.findById(sensor1.getSensorId()).orElse(null);

    assertNotNull(sensor);
  }
}
