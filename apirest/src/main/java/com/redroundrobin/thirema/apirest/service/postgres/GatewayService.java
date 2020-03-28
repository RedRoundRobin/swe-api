package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

  private GatewayRepository repository;

  @Autowired
  public GatewayService(GatewayRepository repository) {
    this.repository = repository;
  }

  public List<Gateway> findAll() {
    return (List<Gateway>) repository.findAll();
  }

  public Gateway find(int gatewayId) {
    return repository.findById(gatewayId).orElse(null);
  }
}