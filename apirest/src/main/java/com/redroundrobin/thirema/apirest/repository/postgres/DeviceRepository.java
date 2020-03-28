package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Integer> {
  Iterable<Device> findAllByGateway(Gateway gateway);

  @Query("SELECT DISTINCT D FROM Entity E " +
      "JOIN E.sensors S JOIN S.device D WHERE E.entityId = ?1")
  Iterable<Device> findAllByEntityId(int entityId);

  Device findBySensors(Sensor sensor);

  @Query("SELECT D FROM Entity E " +
      "JOIN E.sensors S JOIN S.device D WHERE D.deviceId = :id AND E.entityId = :entityId")
  Device findByIdAndEntityId(int id, int entityId);
}