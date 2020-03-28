package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.repository.postgres.DeviceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private DeviceRepository repository;

  @Autowired
  public DeviceService(DeviceRepository repository) {
    this.repository = repository;
  }

  public List<Device> findAll() {
    return (List<Device>) repository.findAll();
  }

  public Device find(int id) {
    return repository.findById(id).orElse(null);
  }
}