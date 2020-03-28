package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

  private GatewayRepository repo;

  private DeviceService deviceService;

  @Autowired
  public GatewayService(GatewayRepository gatewayRepository) {
    this.repo = gatewayRepository;
  }

  @Autowired
  public void setDeviceService(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  public List<Gateway> findAll() {
    return (List<Gateway>) repo.findAll();
  }

  public Gateway findById(int gatewayId) {
    return repo.findById(gatewayId).orElse(null);
  }

  public Gateway findByDeviceId(int deviceId) {
    Device device = deviceService.findById(deviceId);
    if (device != null) {
      return repo.findByDevices(device);
    } else {
      return null;
    }
  }
}