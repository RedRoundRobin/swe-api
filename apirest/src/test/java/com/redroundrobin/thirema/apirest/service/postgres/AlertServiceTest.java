package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
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
public class AlertServiceTest {

  @MockBean
  private AlertRepository alertRepo;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private UserRepository userRepo;


  private AlertService alertService;


  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  private Alert alert1;
  private Alert alert2;
  private Alert alert3;

  private User user1;
  private User user2;
  private User user3;


  @Before
  public void setUp() {
    alertService = new AlertService(alertRepo);
    alertService.setEntityRepository(entityRepo);
    alertService.setSensorRepository(sensorRepo);
    alertService.setUserRepository(userRepo);

    // ----------------------------------------- Set Alerts --------------------------------------
    alert1 = new Alert();
    alert1.setAlertId(1);
    alert2 = new Alert();
    alert2.setAlertId(2);
    alert3 = new Alert();
    alert3.setAlertId(3);

    List<Alert> allAlerts = new ArrayList<>();
    allAlerts.add(alert1);
    allAlerts.add(alert2);
    allAlerts.add(alert3);


    // ----------------------------------------- Set Entities --------------------------------------
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


    // ----------------------------------------- Set Sensors --------------------------------------
    sensor1 = new Sensor();
    sensor1.setId(1);
    sensor2 = new Sensor();
    sensor2.setId(2);
    sensor3 = new Sensor();
    sensor3.setId(3);

    List<Sensor> allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);


    // ----------------------------------------- Set Users --------------------------------------
    user1 = new User();
    user1.setId(1);
    user2 = new User();
    user2.setId(2);
    user3 = new User();
    user3.setId(3);

    List<User> allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(user2);
    allUsers.add(user3);


    // --------------------------- Set Alerts to Entities and viceversa ---------------------------
    List<Alert> entity1Alerts = new ArrayList<>();
    entity1Alerts.add(alert1);
    alert1.setEntity(entity1);
    entity1Alerts.add(alert2);
    alert2.setEntity(entity1);
    entity1.setAlerts(entity1Alerts);

    List<Alert> entity2Alerts = new ArrayList<>();
    entity2Alerts.add(alert3);
    alert3.setEntity(entity2);
    entity2.setAlerts(entity2Alerts);

    entity3.setAlerts(Collections.emptyList());


    // --------------------------- Set Alerts to Sensors and viceversa ---------------------------
    List<Alert> sensor1Alerts = new ArrayList<>();
    sensor1Alerts.add(alert1);
    sensor1Alerts.add(alert2);
    alert1.setSensor(sensor1);
    alert2.setSensor(sensor1);
    sensor1.setAlerts(sensor1Alerts);

    List<Alert> sensor2Alerts = new ArrayList<>();
    sensor2Alerts.add(alert3);
    alert3.setSensor(sensor2);
    sensor2.setAlerts(sensor2Alerts);

    sensor3.setAlerts(Collections.emptyList());


    // --------------------------- Set Alerts to Users and viceversa ---------------------------
    List<Alert> user1Alerts = new ArrayList<>();
    user1Alerts.add(alert1);
    user1.setDisabledAlerts(user1Alerts);
    List<User> alert1Users = new ArrayList<>();
    alert1Users.add(user1);
    alert1.setUsers(alert1Users);

    List<Alert> user2Alerts = new ArrayList<>();
    user2Alerts.add(alert3);
    user2.setDisabledAlerts(user2Alerts);
    List<User> alert3Users = new ArrayList<>();
    alert3Users.add(user2);
    alert3.setUsers(alert3Users);

    user3.setDisabledAlerts(Collections.emptyList());
    alert2.setUsers(Collections.emptyList());



    when(alertRepo.findAll()).thenReturn(allAlerts);
    when(alertRepo.findAllByEntity(any(Entity.class))).thenAnswer(i -> {
      Entity entity = i.getArgument(0);
      return allAlerts.stream().filter(a -> entity.equals(a.getEntity()))
          .collect(Collectors.toList());
    });
    when(alertRepo.findAllBySensor(any(Sensor.class))).thenAnswer(i -> {
      Sensor sensor = i.getArgument(0);
      return allAlerts.stream().filter(a -> sensor.equals(a.getSensor()))
          .collect(Collectors.toList());
    });
    when(alertRepo.findAllByUsers(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return allAlerts.stream().filter(a -> a.getUsers().contains(user))
          .collect(Collectors.toList());
    });
    when(alertRepo.findById(anyInt())).thenAnswer(i -> {
      return allAlerts.stream().filter(a -> i.getArgument(0).equals(a.getAlertId()))
          .findFirst();
    });

    when(entityRepo.findById(anyInt())).thenAnswer(i -> {
      return allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
          .findFirst();
    });

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> {
      return allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst();
    });

    when(userRepo.findById(anyInt())).thenAnswer(i -> {
      return allUsers.stream().filter(u -> i.getArgument(0).equals(u.getId()))
          .findFirst();
    });

  }

  @Test
  public void findAllAlerts() {
    List<Alert> alerts = alertService.findAll();

    assertTrue(!alerts.isEmpty());
  }

  @Test
  public void findAllAlertsByEntityId() {
    List<Alert> alerts = alertService.findAllByEntityId(entity1.getId());

    assertTrue(!alerts.isEmpty());
  }

  @Test
  public void findAllAlertsByNotExistentEntityId() {
    List<Alert> alerts = alertService.findAllByEntityId(4);

    assertTrue(alerts.isEmpty());
  }

  @Test
  public void findAllAlertsBySensorId() {
    List<Alert> alerts = alertService.findAllBySensorId(sensor1.getId());

    assertTrue(alerts.stream().count() == 2);
  }

  @Test
  public void findAllAlertsByNotExistentSensorId() {
    List<Alert> alerts = alertService.findAllBySensorId(4);

    assertTrue(alerts.isEmpty());
  }

  @Test
  public void findAllAlertsByUserId() {
    List<Alert> alerts = alertService.findAllByUserId(user1.getId());

    assertTrue(alerts.stream().count() == 1);
  }

  @Test
  public void findAllAlertsByNotExistentUserId() {
    List<Alert> alerts = alertService.findAllByUserId(4);

    assertTrue(alerts.isEmpty());
  }

  @Test
  public void findAlertById() {
    Alert alert = alertService.findById(alert1.getAlertId());

    assertNotNull(alert);
  }
}
