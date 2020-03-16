package com.redroundrobin.thirema.apirest.repository;

import com.redroundrobin.thirema.apirest.models.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Integer> {

}