package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.timescale.SensorRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service(value = "timescaleSensorService")
public class SensorService {

  private SensorRepository sensorRepo;

  private com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository postgreSensorRepo;

  private EntityRepository entityRepo;

  public SensorService(SensorRepository sensorRepository,
                       com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository
                           postgreSensorRepository, EntityRepository entityRepository) {
    this.sensorRepo = sensorRepository;
    this.postgreSensorRepo = postgreSensorRepository;
    this.entityRepo = entityRepository;
  }

  private Map<Integer, List<Sensor>> findTopNBySensorIdListAndOptionalEntityId(
      Integer limit, List<Integer> sensorIds, Integer entityId) {
    Map<Integer, List<Sensor>> sensorsData = new HashMap<>();

    Entity entity = null;
    if (entityId != null) {
      entity = entityRepo.findById(entityId).orElse(null);
    }

    for (Integer id : sensorIds) {
      com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor;
      if (entityId != null) {
        sensor = postgreSensorRepo.findBySensorIdAndEntities(id, entity);
      } else {
        sensor = postgreSensorRepo.findById(id).orElse(null);
      }

      if (sensor != null) {
        String gatewayName = sensor.getDevice().getGateway().getName();
        int realDeviceId = sensor.getDevice().getId();
        int realSensorId = sensor.getRealSensorId();

        if (limit != null) {
          sensorsData.put(id,
              (List<Sensor>) sensorRepo.findTopNByGatewayNameAndRealDeviceIdAndRealSensorId(
                  limit, gatewayName, realDeviceId, realSensorId));
        } else {
          sensorsData.put(id,
              (List<Sensor>) sensorRepo.findAllByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(
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

    Entity entity = null;
    if (entityId != null) {
      entity = entityRepo.findById(entityId).orElse(null);
    }

    List<com.redroundrobin.thirema.apirest.models.postgres.Sensor> postgreSensors;
    if (entityId != null) {
      postgreSensors = (List<com.redroundrobin.thirema.apirest.models.postgres.Sensor>)
          postgreSensorRepo.findAllByEntities(entity);
    } else {
      postgreSensors = (List<com.redroundrobin.thirema.apirest.models.postgres.Sensor>)
          postgreSensorRepo.findAll();
    }

    for (com.redroundrobin.thirema.apirest.models.postgres.Sensor s : postgreSensors) {
      String gatewayName = s.getDevice().getGateway().getName();
      int realDeviceId = s.getDevice().getRealDeviceId();
      int realSensorId = s.getRealSensorId();
      if (limit != null) {
        sensorsMap.put(s.getId(),
            (List<Sensor>) sensorRepo.findTopNByGatewayNameAndRealDeviceIdAndRealSensorId(limit,
                gatewayName, realDeviceId, realSensorId));
      } else {
        sensorsMap.put(s.getId(),
            (List<Sensor>) sensorRepo.findAllByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(
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

  private Sensor findLastValueBySensorIdAndOptionalEntityId(int sensorId, Integer entityId) {
    com.redroundrobin.thirema.apirest.models.postgres.Sensor sensor;
    if (entityId != null) {
      Entity entity = entityRepo.findById(entityId).orElse(null);
      sensor = postgreSensorRepo.findBySensorIdAndEntities(sensorId, entity);
    } else {
      sensor = postgreSensorRepo.findById(sensorId).orElse(null);
    }
    if (sensor != null) {
      String gatewayName = sensor.getDevice().getGateway().getName();
      int realDeviceId = sensor.getDevice().getRealDeviceId();
      int realSensorId = sensor.getRealSensorId();
      return sensorRepo.findTopByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(gatewayName,
          realDeviceId, realSensorId);
    } else {
      return null;
    }
  }

  public Sensor findLastValueBySensorId(int sensorId) {
    return findLastValueBySensorIdAndOptionalEntityId(sensorId, null);
  }

  public Sensor findLastValueBySensorIdAndEntityId(int sensorId, int entityId) {
    return findLastValueBySensorIdAndOptionalEntityId(sensorId, entityId);
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
}
