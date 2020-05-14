package com.redroundrobin.thirema.apirest.service.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.AlertRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class EntityServiceTest {

  private EntityService entityService;

  @MockBean
  private EntityRepository entityRepo;

  @MockBean
  private AlertRepository alertRepo;

  @MockBean
  private SensorRepository sensorRepo;

  @MockBean
  private UserRepository userRepo;

  private Entity entity1;
  private Entity entity2;
  private Entity entity3;

  private Sensor sensor1;
  private Sensor sensor2;
  private Sensor sensor3;

  private User user1;
  private User user2;

  List<Entity> allEntities;
  List<Sensor> allSensors;
  List<User> allUsers;

  @Before
  public void setUp() {
    entityService = new EntityService(entityRepo, alertRepo, sensorRepo, userRepo);

    // ----------------------------------------- Set sensors --------------------------------------
    sensor1 = new Sensor(1, "type1", 1);
    sensor2 = new Sensor(2, "type2", 2);
    sensor3 = new Sensor(3, "type3", 3);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1);
    allSensors.add(sensor2);
    allSensors.add(sensor3);

    // ----------------------------------------- Set entities --------------------------------------
    entity1 = new Entity(1, "entity1", "loc1");
    entity2 = new Entity(2, "entity2", "loc2");
    entity3 = new Entity(3, "entity3", "loc3");

    allEntities = new ArrayList<>();
    allEntities.add(entity1);
    allEntities.add(entity2);
    allEntities.add(entity3);

    // ----------------------------------------- Set users --------------------------------------
    user1 = new User(1, "name1", "surname1", "email1", "pass1", User.Role.USER);
    user2 = new User(2, "name2", "surname2", "email2", "pass2", User.Role.USER);

    allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(user2);

    // -------------------------------- Set sensors to entities ----------------------------------
    Set<Sensor> sensors1 = new HashSet<>();
    sensors1.add(sensor1);
    sensors1.add(sensor2);
    entity1.setSensors(sensors1);
    entity3.setSensors(sensors1);

    Set<Sensor> sensors2 = new HashSet<>();
    sensors2.add(sensor3);
    entity2.setSensors(sensors2);

    // -------------------------------- Set entities to users -----------------------------------
    user1.setEntity(entity1);

    user2.setEntity(entity2);

    when(entityRepo.findAll()).thenReturn(allEntities);
    when(entityRepo.findAllBySensors(any(Sensor.class))).thenAnswer(i -> allEntities.stream().filter(e -> e.getSensors().contains(i.getArgument(0)))
        .collect(Collectors.toList()));
    when(entityRepo.findAllByUsers(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return allEntities.stream().filter(e -> user.getEntity().equals(e))
          .collect(Collectors.toList());
    });
    when(entityRepo.findAllBySensorsAndUsers(any(Sensor.class), any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(1);
      return allEntities.stream().filter(e -> e.getSensors().contains(i.getArgument(0))
          && user.getEntity().equals(e))
          .collect(Collectors.toList());
    });
    when(entityRepo.findById(anyInt())).thenAnswer(i -> allEntities.stream().filter(e -> i.getArgument(0).equals(e.getId()))
        .findFirst());
    when(entityRepo.save(any(Entity.class))).thenAnswer(i -> i.getArgument(0));

    doNothing().when(alertRepo).deleteAlertsBySensor(any(Sensor.class));

    when(sensorRepo.findById(anyInt())).thenAnswer(i -> allSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
        .findFirst());

    when(userRepo.findById(anyInt())).thenAnswer(i -> allUsers.stream().filter(u -> i.getArgument(0).equals(u.getId())).findFirst());
  }

  @Test
  public void findAllEntities() {
    List<Entity> entities = entityService.findAll();

    assertFalse(entities.isEmpty());
  }

  @Test
  public void findAllEntitiesBySensorId() {
    List<Entity> entities = entityService.findAllBySensorId(sensor1.getId());

    assertEquals(2, (long) entities.size());
  }

  @Test
  public void findAllEntitiesBySensorIdEmptyResult() {
    List<Entity> entities = entityService.findAllBySensorId(4);

    assertEquals(0, (long) entities.size());
  }

  @Test
  public void findAllEntitiesByUserId() {
    List<Entity> entities = entityService.findAllByUserId(sensor1.getId());

    assertEquals(1, (long) entities.size());
  }

  @Test
  public void findAllEntitiesByUserIdEmptyResult() {
    List<Entity> entities = entityService.findAllByUserId(4);

    assertEquals(0, (long) entities.size());
  }

  @Test
  public void findAllEntitiesBySensorIdAndUserId() {
    List<Entity> entities = entityService.findAllBySensorIdAndUserId(sensor1.getId(), user1.getId());

    assertEquals(1, (long) entities.size());
  }

  @Test
  public void findAllEntitiesBySensorIdAndUserIdEmptyResult() {
    List<Entity> entities = entityService.findAllBySensorIdAndUserId(4, 4);

    assertEquals(0, (long) entities.size());
  }



  @Test
  public void findEntityById() {
    Entity entity = entityService.findById(entity1.getId());

    assertNotNull(entity);
  }



  @Test
  public void addEntitySuccessfull() {
    String name = "nome";
    String location = "locazione";

    Map<String, Object> newEntityFields = new HashMap<>();
    newEntityFields.put("name", name);
    newEntityFields.put("location", location);

    try {
      Entity newEntity = entityService.addEntity(newEntityFields);

      assertEquals(name, newEntity.getName());
      assertEquals(location, newEntity.getLocation());
    } catch (MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void addEntityWithMissingFieldsException() {
    String name = "nome";

    Map<String, Object> newEntityFields = new HashMap<>();
    newEntityFields.put("name", name);

    try {
      Entity newEntity = entityService.addEntity(newEntityFields);

      assertEquals(name, newEntity.getName());
      assertTrue(false);
    } catch (MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(true);
    }
  }



  @Test
  public void editEntitySuccessfull() {
    String name = "nome";
    String location = "locazione";

    Map<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name", name);
    fieldsToEdit.put("location", location);

    try {
      Entity editedEntity = entityService.editEntity(entity2.getId(), fieldsToEdit);

      assertEquals(entity2.getId(), editedEntity.getId());
      assertEquals(name, editedEntity.getName());
      assertEquals(location, editedEntity.getLocation());
    } catch (MissingFieldsException | InvalidFieldsValuesException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void editEntityWithMissingFieldsException() {

    Map<String, Object> fieldsToEdit = new HashMap<>();

    try {
      Entity editedEntity = entityService.editEntity(entity1.getId(), fieldsToEdit);

      assertTrue(false);
    } catch (MissingFieldsException e) {
      assertTrue(true);
    } catch (InvalidFieldsValuesException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void editEntityByNotExistentEntityIdThrowInvalidFieldsValuesException() {
    String name = "nome";
    String location = "locazione";

    Map<String, Object> fieldsToEdit = new HashMap<>();
    fieldsToEdit.put("name", name);
    fieldsToEdit.put("location", location);

    try {
      Entity editedEntity = entityService.editEntity(10, fieldsToEdit);

      assertTrue(false);
    } catch (MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (InvalidFieldsValuesException e) {
      assertTrue(true);
    }
  }



  @Test
  public void deleteEntitySuccessfull() {
    doNothing().when(userRepo).setDeletedByEntity(entity1);
    doNothing().when(alertRepo).setDeletedByEntity(entity1);

    try {
      boolean deleted = entityService.deleteEntity(entity1.getId());

      assertTrue(deleted);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteEntitySimulateDbError() {
    doNothing().when(userRepo).setDeletedByEntity(entity1);
    doNothing().when(alertRepo).setDeletedByEntity(entity1);

    Entity entity3Bis = new Entity(19, entity3.getName(), entity3.getLocation());
    entity3Bis.setDeleted(true);

    when(entityRepo.findById(entity3Bis.getId())).thenReturn(Optional.of(entity3Bis));
    when(entityRepo.save(entity3Bis)).thenReturn(entity3);

    try {
      boolean deleted = entityService.deleteEntity(entity3Bis.getId());

      assertFalse(deleted);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void deleteEntityByNotExistentEntityIdThrowElementNotFoundException() {

    try {
      boolean deleted = entityService.deleteEntity(10);

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      assertTrue(true);
    }
  }



  @Test
  public void enableOrDisableSensorToEntitySuccessfull() {
    try {
      Map<String, Object> fieldsToEdit = new HashMap<>();
      List<Integer> list = new ArrayList<>();
      list.add(sensor3.getId());
      fieldsToEdit.put("toInsert", list);
      fieldsToEdit.put("toDelete", list);
      boolean edited = entityService.enableOrDisableSensorToEntity(entity1.getId(), fieldsToEdit);

      assertTrue(edited);
    } catch (ElementNotFoundException | MissingFieldsException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void enableOrDisableSensorToEntityThrowMissingFieldsException() {
    try {
      boolean edited = entityService.enableOrDisableSensorToEntity(entity1.getId(), Collections.emptyMap());

      assertTrue(false);
    } catch (ElementNotFoundException e) {
      e.printStackTrace();
      assertTrue(false);
    } catch (MissingFieldsException e) {
      assertTrue(true);
    }
  }

  @Test
  public void enableOrDisableSensorToEntityByNotExistentEntityIdThrowElementNotFoundException() {
    try {
      boolean edited = entityService.enableOrDisableSensorToEntity(10,
          Collections.emptyMap());

      assertTrue(false);
    } catch (ElementNotFoundException | MissingFieldsException e) {
      assertTrue(true);
    }
  }
}
