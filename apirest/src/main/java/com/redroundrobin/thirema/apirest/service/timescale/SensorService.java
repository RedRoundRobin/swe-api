package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.repository.timescale.SensorRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service(value = "timescaleSensorService")
public class SensorService {

  private SensorRepository repo;

  private com.redroundrobin.thirema.apirest.service.postgres.SensorService postgreSensorService;

  @Autowired
  public SensorService(@Qualifier("timescaleSensorRepository") SensorRepository sensorRepository) {
    this.repo = sensorRepository;
  }

  private Map<Integer, List<Sensor>> findTopNBySensorIdListAndOptionalEntityId(
      Integer limit, List<Integer> sensorIds, Integer entityId) {
    Map<Integer, List<Sensor>> sensorsData = new HashMap<>();

    for (Integer id : sensorIds) {
      com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor;
      if (entityId != null) {
        sensor = postgreSensorService.findByIdAndEntityId(id, entityId);
      } else {
        sensor = postgreSensorService.findById(id);
      }

      if (sensor != null) {
        String gatewayName = sensor.getDevice().getGateway().getName();
        int realDeviceId = sensor.getDevice().getId();
        int realSensorId = sensor.getRealSensorId();

        if (limit != null) {
          sensorsData.put(id,
              (List<Sensor>) repo.findTopNByGatewayNameAndRealDeviceIdAndRealSensorId(
                  limit, gatewayName, realDeviceId, realSensorId));
        } else {
          sensorsData.put(id,
              (List<Sensor>) repo.findAllByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(
                  gatewayName, realDeviceId, realSensorId));
        }
      } else {
        sensorsData.put(id, Collections.emptyList());
      }
    }

    return sensorsData;
  }

  private Map<Integer, List<Sensor>> findTopNForEachSensorByOptionalEntity(Integer limit,
                                                                           Integer entityId) {
    Map<Integer, List<Sensor>> sensorsMap = new HashMap<>();

    List<com.redroundrobin.thirema.apirest.models.postgres.Sensor> postgreSensors;
    if (entityId != null) {
      postgreSensors = postgreSensorService.findAllByEntityId(entityId);
    } else {
      postgreSensors = postgreSensorService.findAll();
    }

    for (com.redroundrobin.thirema.apirest.models.postgres.Sensor s : postgreSensors) {
      String gatewayName = s.getDevice().getGateway().getName();
      int realDeviceId = s.getDevice().getRealDeviceId();
      int realSensorId = s.getRealSensorId();
      if (limit != null) {
        sensorsMap.put(s.getId(),
            (List<Sensor>) repo.findTopNByGatewayNameAndRealDeviceIdAndRealSensorId(limit,
                gatewayName, realDeviceId, realSensorId));
      } else {
        sensorsMap.put(s.getId(),
            (List<Sensor>) repo.findAllByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(
                gatewayName, realDeviceId, realSensorId));
      }
    }

    return sensorsMap;
  }

  public Map<Integer, List<Sensor>> findAllBySensorIdList(List<Integer> sensorIds) {
    return findTopNBySensorIdListAndOptionalEntityId(null, sensorIds, null);
  }

  public Map<Integer, List<Sensor>> findAllBySensorIdListAndEntityId(List<Integer> sensorIds,
                                                                     int entityId) {
    return findTopNBySensorIdListAndOptionalEntityId(null, sensorIds, entityId);
  }

  public Map<Integer, List<Sensor>> findAllForEachSensor() {
    return findTopNForEachSensorByOptionalEntity(null, null);
  }

  public Map<Integer, List<Sensor>> findAllForEachSensorByEntityId(int entityId) {
    return findTopNForEachSensorByOptionalEntity(null, entityId);
  }

  public Sensor findLastValueBySensorId(int sensorId) {
    com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor =
        postgreSensorService.findById(sensorId);
    if (sensor != null) {
      String gatewayName = sensor.getDevice().getGateway().getName();
      int realDeviceId = sensor.getDevice().getRealDeviceId();
      int realSensorId = sensor.getRealSensorId();
      return repo.findTopByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(gatewayName,
          realDeviceId, realSensorId);
    } else {
      return null;
    }
  }

  public Sensor findLastValueBySensorIdAndEntityId(int sensorId, int entityId) {
    com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor =
        postgreSensorService.findById(sensorId);
    if (sensor != null && sensor.getEntities().stream().anyMatch(e -> e.getId() == entityId)) {
      String gatewayName = sensor.getDevice().getGateway().getName();
      int realDeviceId = sensor.getDevice().getRealDeviceId();
      int realSensorId = sensor.getRealSensorId();
      return repo.findTopByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(gatewayName,
          realDeviceId, realSensorId);
    } else {
      return null;
    }
  }

  public Map<Integer, List<Sensor>> findTopNBySensorIdList(int limit,
                                                           List<Integer> sensorIds) {
    return findTopNBySensorIdListAndOptionalEntityId(limit, sensorIds, null);
  }

  public Map<Integer, List<Sensor>> findTopNBySensorIdListAndEntityId(int limit,
                                                                      List<Integer> sensorIds,
                                                                      int entityId) {
    return findTopNBySensorIdListAndOptionalEntityId(limit, sensorIds, entityId);
  }

  public Map<Integer, List<Sensor>> findTopNForEachSensor(int limit) {
    return findTopNForEachSensorByOptionalEntity(limit, null);
  }

  public Map<Integer, List<Sensor>> findTopNForEachSensorByEntityId(int limit, int entityId) {
    return findTopNForEachSensorByOptionalEntity(limit, entityId);
  }

  @Autowired
  public void setPostgreSensorService(
      com.redroundrobin.thirema.apirest.service.postgres.SensorService postgreSensorService) {
    this.postgreSensorService = postgreSensorService;
  }

}
