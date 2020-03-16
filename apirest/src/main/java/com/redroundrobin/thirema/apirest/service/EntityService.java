package com.redroundrobin.thirema.apirest.service;

import java.util.List;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

    @Autowired
    private EntityRepository repository;

    public List<Entity> findAll() {
        return (List<Entity>) repository.findAll();
    }
    public Entity find(int id){
        return repository.findById(id).get();
    }
}