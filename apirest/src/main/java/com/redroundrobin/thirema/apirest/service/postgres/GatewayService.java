package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

  private GatewayRepository gatewayRepo;

  private DeviceRepository deviceRepo;

  @Autowired
  public GatewayService(GatewayRepository gatewayRepository, DeviceRepository deviceRepository) {
    this.gatewayRepo = gatewayRepository;
    this.deviceRepo = deviceRepository;
  }

  public List<Gateway> findAll() {
    return (List<Gateway>) gatewayRepo.findAll();
  }

  public List<Gateway> findAllByEntityId(int entityId) {
    return (List<Gateway>) gatewayRepo.findAllByEntityId(entityId);
  }

  public Gateway findByDeviceId(int deviceId) {
    return gatewayRepo.findByDevice(deviceId);
  }

  public Gateway findByDeviceIdAndEntityId(int deviceId, int entityId) {
    return gatewayRepo.findByDeviceIdAndEntityId(deviceId, entityId);
  }

  public Gateway findById(int id) {
    return gatewayRepo.findById(id).orElse(null);
  }

}