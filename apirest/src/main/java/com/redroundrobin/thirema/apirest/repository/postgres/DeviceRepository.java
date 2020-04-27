package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Integer> {
  Iterable<Device> findAllByGateway(Gateway gateway);

  @Query("SELECT DISTINCT D FROM Entity E "
      + "JOIN E.sensors S JOIN S.device D WHERE E.entityId = ?1")
  Iterable<Device> findAllByEntityId(int entityId);

  @Query("SELECT DISTINCT D FROM Device D "
      + "JOIN D.gateway G WHERE G.gatewayId = :gatewayId AND D.gateway.gatewayId = :gatewayId")
  Iterable<Device> findAllByGatewayId(int gatewayId);

  @Query("SELECT DISTINCT S FROM Sensor S "
      + "JOIN S.device D WHERE D.deviceId = :deviceId AND S.device.deviceId = :deviceId")
  Iterable<Sensor> findAllByDeviceId(int deviceId);

  @Query("SELECT DISTINCT D FROM Entity E "
      + "JOIN E.sensors S JOIN S.device D WHERE E.entityId = :entityId AND D.gateway = :gateway")
  Iterable<Device> findAllByEntityIdAndGateway(int entityId, Gateway gateway);

  @Query("SELECT D FROM Sensor S JOIN S.device D WHERE S = :sensor")
  Device findBySensors(Sensor sensor);

  @Query("SELECT DISTINCT D FROM Sensor S JOIN S.device D WHERE S.cmdEnabled = :cmdEnabled")
  Iterable<Device> findBySensorsCmdEnabledField(boolean cmdEnabled);

  @Query("SELECT DISTINCT S FROM Sensor S JOIN S.device D WHERE S.cmdEnabled = :cmdEnabled "
      + "AND D.deviceId = :deviceId")
  Iterable<Sensor> findByCmdEnabledAndDeviceId(boolean cmdEnabled, int deviceId);

  @Query("SELECT D FROM Entity E "
      + "JOIN E.sensors S JOIN S.device D WHERE D.deviceId = :id AND E.entityId = :entityId")
  Device findByIdAndEntityId(int id, int entityId);

  Device findByGatewayAndRealDeviceId(Gateway gateway, int realDeviceId);
}