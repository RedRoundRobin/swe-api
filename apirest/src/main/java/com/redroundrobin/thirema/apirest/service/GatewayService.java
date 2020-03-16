package com.redroundrobin.thirema.apirest.service;

import java.util.List;

import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.repository.postgres.GatewayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayService {

    @Autowired
    private GatewayRepository repository;

    public List<Gateway> findAll() {
        return  (List<Gateway>) repository.findAll();
    }
    public Gateway find(int gatewayId){
        return repository.findById(gatewayId).get();
    }
}