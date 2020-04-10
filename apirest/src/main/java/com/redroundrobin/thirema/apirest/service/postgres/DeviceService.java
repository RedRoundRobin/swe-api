package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.SensorRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private DeviceRepository deviceRepo;

  private GatewayRepository gatewayRepo;

  private SensorRepository sensorRepo;

  private EntityRepository entityRepo;

  @Autowired
  public DeviceService(DeviceRepository deviceRepository, EntityRepository entityRepository,
                       GatewayRepository gatewayRepository, SensorRepository sensorRepository) {
    this.deviceRepo = deviceRepository;
    this.entityRepo = entityRepository;
    this.gatewayRepo = gatewayRepository;
    this.sensorRepo = sensorRepository;
  }

  public List<Device> findAll() {
    return (List<Device>) deviceRepo.findAll();
  }

  public List<Device> findAllByEntityId(int entityId) {
    return (List<Device>) deviceRepo.findAllByEntityId(entityId);
  }

  public List<Device> findAllByEntityIdAndGatewayId(int entityId, int gatewayId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return (List<Device>) deviceRepo.findAllByEntityIdAndGateway(entityId, gateway);
    } else {
      return Collections.emptyList();
    }
  }

  public List<Device> findAllByGatewayId(int gatewayId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return (List<Device>) deviceRepo.findAllByGateway(gateway);
    } else {
      return Collections.emptyList();
    }
  }

  public Device findById(int id) {
    return deviceRepo.findById(id).orElse(null);
  }

  public Device findBySensorId(int sensorId) {
    Sensor sensor = sensorRepo.findById(sensorId).orElse(null);
    if (sensor != null) {
      return deviceRepo.findBySensors(sensor);
    } else {
      return null;
    }
  }

  public Device findByIdAndEntityId(int id, int entityId) {
    return deviceRepo.findByIdAndEntityId(id, entityId);
  }

  public Device findByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId) {
    Gateway gateway = gatewayRepo.findById(gatewayId).orElse(null);
    if (gateway != null) {
      return deviceRepo.findByGatewayAndRealDeviceId(gateway, realDeviceId);
    } else {
      return null;
    }
  }
}