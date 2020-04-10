package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRepository extends CrudRepository<Gateway, Integer> {

  @Query("SELECT DISTINCT G FROM Entity E JOIN E.sensors S JOIN S.device D JOIN D.gateway G "
      + "WHERE E.entityId = :entityId")
  Iterable<Gateway> findAllByEntityId(int entityId);

  @Query("SELECT G FROM Device D JOIN D.gateway G WHERE D.deviceId = :deviceId")
  Gateway findByDevice(int deviceId);

  @Query("SELECT G FROM Entity E JOIN E.sensors S JOIN S.device D JOIN D.gateway G "
      + "WHERE D = :deviceId AND E = :entityId")
  Gateway findByDeviceIdAndEntityId(int deviceId, int entityId);

  @Query("SELECT G FROM Entity E JOIN E.sensors S JOIN S.device D JOIN D.gateway G "
      + "WHERE G.gatewayId = :id AND E.entityId = :entityId")
  Gateway findByIdAndEntityId(int id, int entityId);
}