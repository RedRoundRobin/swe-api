package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.repository.timescale.SensorRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SensorServiceTest {

  @MockBean
  private SensorRepository repo;

  @MockBean
  private com.redroundrobin.thirema.apirest.service.postgres.SensorService postgreSensorService;

  private SensorService sensorService;

  Sensor sensor1111;
  Sensor sensor1112;
  Sensor sensor1113;
  Sensor sensor1121;
  Sensor sensor1122;
  Sensor sensor1123;

  Sensor sensor1211;
  Sensor sensor1212;
  Sensor sensor1213;
  Sensor sensor1221;
  Sensor sensor1222;
  Sensor sensor1223;

  List<Sensor> allSensors;

  com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor1;
  com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor2;
  com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor3;
  com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor4;
  List<com.redroundrobin.thirema.apirest.models.postgres.Sensor> allPostgreSensors;

  List<Sensor> allG1D1S1Sensors;
  List<Sensor> allG1D1S2Sensors;
  List<Sensor> allG1D2S1Sensors;
  List<Sensor> allG1D2S2Sensors;


  @Before
  public void setUp() {
    sensorService = new SensorService(repo);
    sensorService.setPostgreSensorService(postgreSensorService);


    // ------------------------------------ Set Timescale Sensors ---------------------------------
    sensor1111 = new Sensor();
    sensor1111.setTime(new Timestamp(100));
    sensor1111.setGatewayId(1);
    sensor1111.setDeviceId(1);
    sensor1111.setSensorId(1);
    sensor1111.setValue(1);
    sensor1112 = new Sensor();
    sensor1112.setTime(new Timestamp(200));
    sensor1112.setGatewayId(1);
    sensor1112.setDeviceId(1);
    sensor1112.setSensorId(1);
    sensor1112.setValue(2);
    sensor1113 = new Sensor();
    sensor1113.setTime(new Timestamp(300));
    sensor1113.setGatewayId(1);
    sensor1113.setDeviceId(1);
    sensor1113.setSensorId(1);
    sensor1113.setValue(3);

    sensor1121 = new Sensor();
    sensor1121.setTime(new Timestamp(100));
    sensor1121.setGatewayId(1);
    sensor1121.setDeviceId(1);
    sensor1121.setSensorId(2);
    sensor1121.setValue(1);
    sensor1122 = new Sensor();
    sensor1122.setTime(new Timestamp(200));
    sensor1122.setGatewayId(1);
    sensor1122.setDeviceId(1);
    sensor1122.setSensorId(2);
    sensor1122.setValue(2);
    sensor1123 = new Sensor();
    sensor1123.setTime(new Timestamp(300));
    sensor1123.setGatewayId(1);
    sensor1123.setDeviceId(1);
    sensor1123.setSensorId(2);
    sensor1123.setValue(3);

    sensor1211 = new Sensor();
    sensor1211.setTime(new Timestamp(100));
    sensor1211.setGatewayId(1);
    sensor1211.setDeviceId(2);
    sensor1211.setSensorId(1);
    sensor1211.setValue(1);
    sensor1212 = new Sensor();
    sensor1212.setTime(new Timestamp(200));
    sensor1212.setGatewayId(1);
    sensor1212.setDeviceId(2);
    sensor1212.setSensorId(1);
    sensor1212.setValue(2);
    sensor1213 = new Sensor();
    sensor1213.setTime(new Timestamp(300));
    sensor1213.setGatewayId(1);
    sensor1213.setDeviceId(2);
    sensor1213.setSensorId(1);
    sensor1213.setValue(3);

    sensor1221 = new Sensor();
    sensor1221.setTime(new Timestamp(100));
    sensor1221.setGatewayId(1);
    sensor1221.setDeviceId(2);
    sensor1221.setSensorId(2);
    sensor1221.setValue(1);
    sensor1222 = new Sensor();
    sensor1222.setTime(new Timestamp(200));
    sensor1222.setGatewayId(1);
    sensor1222.setDeviceId(2);
    sensor1222.setSensorId(2);
    sensor1222.setValue(2);
    sensor1223 = new Sensor();
    sensor1223.setTime(new Timestamp(300));
    sensor1223.setGatewayId(1);
    sensor1223.setDeviceId(2);
    sensor1223.setSensorId(2);
    sensor1223.setValue(3);

    allSensors = new ArrayList<>();
    allSensors.add(sensor1111);
    allSensors.add(sensor1112);
    allSensors.add(sensor1113);
    allSensors.add(sensor1121);
    allSensors.add(sensor1122);
    allSensors.add(sensor1123);
    allSensors.add(sensor1211);
    allSensors.add(sensor1212);
    allSensors.add(sensor1213);
    allSensors.add(sensor1221);
    allSensors.add(sensor1222);
    allSensors.add(sensor1223);

    // all gateway 1 device 1 sensor 1 sensors
    allG1D1S1Sensors = new ArrayList<>();
    allG1D1S1Sensors.add(sensor1111);
    allG1D1S1Sensors.add(sensor1112);
    allG1D1S1Sensors.add(sensor1113);

    // all gateway 1 device 1 sensor 2 sensors
    allG1D1S2Sensors = new ArrayList<>();
    allG1D1S2Sensors.add(sensor1121);
    allG1D1S2Sensors.add(sensor1122);
    allG1D1S2Sensors.add(sensor1123);

    // all gateway 1 device 2 sensor 1 sensors
    allG1D2S1Sensors = new ArrayList<>();
    allG1D2S1Sensors.add(sensor1211);
    allG1D2S1Sensors.add(sensor1212);
    allG1D2S1Sensors.add(sensor1213);

    // all gateway 1 device 2 sensor 2 sensors
    allG1D2S2Sensors = new ArrayList<>();
    allG1D2S2Sensors.add(sensor1221);
    allG1D2S2Sensors.add(sensor1222);
    allG1D2S2Sensors.add(sensor1223);


    // -------------------------------------- Set Gateway ----------------------------------------
    Gateway gateway1 = new Gateway();
    gateway1.setId(1);


    // -------------------------------------- Set Devices ----------------------------------------
    Device device1 = new Device();
    device1.setId(1);
    device1.setRealDeviceId(1);
    device1.setGateway(gateway1);

    Device device2 = new Device();
    device2.setId(2);
    device2.setRealDeviceId(2);
    device2.setGateway(gateway1);

    List<Device> allDevices = new ArrayList<>();
    allDevices.add(device1);
    allDevices.add(device2);


    // -------------------------------------- Set Devices ----------------------------------------
    Entity entity1 = new Entity();
    entity1.setId(1);

    List<Entity> sensor1Entities = new ArrayList<>();
    sensor1Entities.add(entity1);


    // ----------------------------------- Set Postgre Sensors ------------------------------------
    sensor1 = new com.redroundrobin.thirema.apirest.models.postgres.Sensor();
    sensor1.setId(1);
    sensor1.setRealSensorId(1);
    sensor1.setDevice(device1);
    sensor1.setEntities(sensor1Entities);

    sensor2 = new com.redroundrobin.thirema.apirest.models.postgres.Sensor();
    sensor2.setId(2);
    sensor2.setRealSensorId(2);
    sensor2.setDevice(device1);
    sensor2.setEntities(Collections.emptyList());

    sensor3 = new com.redroundrobin.thirema.apirest.models.postgres.Sensor();
    sensor3.setId(3);
    sensor3.setRealSensorId(1);
    sensor3.setDevice(device2);
    sensor3.setEntities(Collections.emptyList());

    sensor4 = new com.redroundrobin.thirema.apirest.models.postgres.Sensor();
    sensor4.setId(4);
    sensor4.setRealSensorId(2);
    sensor4.setDevice(device2);
    sensor4.setEntities(Collections.emptyList());

    allPostgreSensors = new ArrayList<>();
    allPostgreSensors.add(sensor1);
    allPostgreSensors.add(sensor2);
    allPostgreSensors.add(sensor3);
    allPostgreSensors.add(sensor4);

    // sensor 1 & 3 are entity 1 sensors, 2 & 4 are entity 2 sensor


    when(repo.findAllByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(anyInt(), anyInt(),
        anyInt())).thenAnswer(i -> {
          int gatewayId = i.getArgument(0);
          int deviceId = i.getArgument(1);
          int sensorId = i.getArgument(2);
          return allSensors.stream().filter(s -> s.getGatewayId() == gatewayId
              && s.getDeviceId() == deviceId && s.getSensorId() == sensorId)
              .sorted((t1,t2) -> Long.compare(t2.getTime().getTime(),t1.getTime().getTime()))
              .collect(Collectors.toList());
    });
    when(repo.findTopNByGatewayIdAndDeviceIdAndSensorId(anyInt(), anyInt(), anyInt(), anyInt()))
        .thenAnswer(i -> {
      int limit = i.getArgument(0);
      int gatewayId = i.getArgument(1);
      int deviceId = i.getArgument(2);
      int sensorId = i.getArgument(3);
      return allSensors.stream().filter(s -> s.getGatewayId() == gatewayId
          && s.getDeviceId() == deviceId && s.getSensorId() == sensorId)
          .sorted((t1,t2) -> Long.compare(t2.getTime().getTime(),t1.getTime().getTime()))
          .limit(limit).collect(Collectors.toList());
    });
    when(repo.findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(anyInt(), anyInt(),
        anyInt())).thenAnswer(i -> {
      int gatewayId = i.getArgument(0);
      int deviceId = i.getArgument(1);
      int sensorId = i.getArgument(2);
      return allSensors.stream().filter(s -> s.getGatewayId() == gatewayId
          && s.getDeviceId() == deviceId && s.getSensorId() == sensorId)
          .sorted((t1,t2) -> Long.compare(t2.getTime().getTime(),t1.getTime().getTime()))
          .findFirst().orElse(null);
    });

    when(postgreSensorService.findAll()).thenReturn(allPostgreSensors);
    when(postgreSensorService.findAllByEntityId(anyInt())).thenAnswer(i -> {
      if (i.getArgument(0).equals(1)) {
        return allPostgreSensors.stream().filter(s -> s.getId() == 1 || s.getId() == 3)
            .collect(Collectors.toList());
      } else if (i.getArgument(0).equals(2)) {
        return allPostgreSensors.stream().filter(s -> s.getId() == 2 || s.getId() == 4)
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    });
    when(postgreSensorService.findByIdAndEntityId(anyInt(), anyInt())).thenAnswer(i -> {
      if (i.getArgument(1).equals(1)) {
        return allPostgreSensors.stream().filter(s -> i.getArgument(0).equals(s.getId())
            && (s.getId() == 1 || s.getId() == 3))
            .findFirst().orElse(null);
      } else if (i.getArgument(1).equals(2)) {
        return allPostgreSensors.stream().filter(s -> i.getArgument(0).equals(s.getId())
            && (s.getId() == 2 || s.getId() == 4))
            .findFirst().orElse(null);
      } else {
        return Collections.emptyList();
      }
    });
    when(postgreSensorService.findById(anyInt())).thenAnswer(i -> {
      return allPostgreSensors.stream().filter(s -> i.getArgument(0).equals(s.getId()))
          .findFirst().orElse(null);
    });
  }

  @Test
  public void findAllForEachSensorSuccessfull() {
    Map<Integer, List<Sensor>> sensors = sensorService.findAllForEachSensor();

    System.out.println(sensors);
    assertEquals(4, sensors.keySet().size());
    sensors.entrySet().forEach(e -> System.out.println(e.getValue().size()));
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }



  @Test
  public void findAllForEachSensorByEntityIdSuccessfull() {
    Map<Integer, List<Sensor>> sensors = sensorService.findAllForEachSensorByEntityId(1);

    assertEquals(2, sensors.keySet().size());
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }



  @Test
  public void findTopNForEachSensorSuccessfull() {
    Map<Integer, List<Sensor>> sensors = sensorService.findTopNForEachSensor(2);

    assertEquals(4, sensors.keySet().size());
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 2));
  }



  @Test
  public void findTopNForEachSensorByEntityIdSuccessfull() {
    Map<Integer, List<Sensor>> sensors = sensorService.findTopNForEachSensorByEntityId(1, 2);

    assertEquals(2, sensors.keySet().size());
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 1));
  }



  @Test
  public void findAllBySensorIdListSuccessfull() {
    List<Integer> sensorIds = new ArrayList<>();
    sensorIds.add(1);
    sensorIds.add(4);
    Map<Integer, List<Sensor>> sensors = sensorService.findAllBySensorIdList(sensorIds);

    assertEquals(2, sensors.keySet().size());
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 3));
  }



  @Test
  public void findAllBySensorIdListAndByEntityIdEmpty() {
    List<Integer> sensorIds = new ArrayList<>();
    sensorIds.add(1);
    sensorIds.add(4);
    Map<Integer, List<Sensor>> sensors = sensorService.findAllBySensorIdListAndEntityId(sensorIds, 1);

    assertEquals(2, sensors.keySet().size());
    assertTrue(sensors.get(1).size() == 3);
    assertTrue(sensors.get(4).isEmpty());
  }



  @Test
  public void findTopNBySensorIdListSuccessfull() {
    List<Integer> sensorIds = new ArrayList<>();
    sensorIds.add(1);
    sensorIds.add(4);
    Map<Integer, List<Sensor>> sensors = sensorService.findTopNBySensorIdList(1, sensorIds);

    assertEquals(2, sensors.keySet().size());
    assertTrue(sensors.entrySet().stream().allMatch(e -> e.getValue().size() == 1));
  }



  @Test
  public void findTopNBySensorIdListAndEntityIdSuccessfull() {
    List<Integer> sensorIds = new ArrayList<>();
    sensorIds.add(1);
    Map<Integer, List<Sensor>> sensors = sensorService.findTopNBySensorIdListAndEntityId(1, sensorIds, 1);

    assertEquals(1, sensors.keySet().size());
    assertTrue(sensors.get(1).size() == 1);
  }


  @Test
  public void findTopBySensorId() {
    Sensor sensor = sensorService.findLastValueBySensorId(1);

    assertNotNull(sensor);
  }

  @Test
  public void findTopByNotExistentSensorId() {
    Sensor sensor = sensorService.findLastValueBySensorId(15);

    assertNull(sensor);
  }


  @Test
  public void findTopBySensorIdAndEntityId() {
    Sensor sensor = sensorService.findLastValueBySensorIdAndEntityId(1, 1);

    assertNotNull(sensor);
  }

  @Test
  public void findTopBySensorIdAndNotExistentEntityId() {
    Sensor sensor = sensorService.findLastValueBySensorIdAndEntityId(1,15);

    assertNull(sensor);
  }
}
