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

  public Gateway findByDeviceId(int deviceId) {
    Device device = deviceRepo.findById(deviceId).orElse(null);
    if (device != null) {
      return gatewayRepo.findByDevices(device);
    } else {
      return null;
    }
  }

  public Gateway findById(int id) {
    return gatewayRepo.findById(id).orElse(null);
  }

}