package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Alert;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends CrudRepository<Sensor, Integer> {
  Iterable<Sensor>  findAllByDevice(Device device);

  @Query("SELECT S FROM Entity E JOIN E.sensors S WHERE E = :entity")
  Iterable<Sensor> findAllByEntities(Entity entity);

  @Query("SELECT S1, S2 FROM ViewGraph VG JOIN VG.sensor1 S1 JOIN VG.sensor2 S2 "
      + "WHERE VG = :viewGraph1 OR VG = :viewGraph2")
  Iterable<Sensor> findAllByViewGraphs1OrViewGraphs2(ViewGraph viewGraph1, ViewGraph viewGraph2);

  @Query("SELECT S FROM Entity E JOIN E.sensors S JOIN S.device D "
      + "WHERE D = :device AND E = :entity")
  Iterable<Sensor> findAllByDeviceAndEntities(Device device, Entity entity);

  @Query("SELECT S FROM Sensor S JOIN S.device D JOIN D.gateway G WHERE G.gatewayId = :gatewayId "
      + "AND D.realDeviceId = :realDeviceId")
  Iterable<Sensor> findAllByGatewayIdAndRealDeviceId(int gatewayId, int realDeviceId);

  @Query("SELECT S FROM Alert A JOIN A.sensor S WHERE A = :alert")
  Sensor findByAlerts(Alert alert);

  Sensor findByDeviceAndRealSensorId(Device device, int realSensorId);

  @Query("SELECT S FROM Entity E JOIN E.sensors S JOIN S.device D WHERE D = :device "
      + "AND S.realSensorId = :realSensorId AND E = :entity")
  Sensor findByDeviceAndRealSensorIdAndEntities(Device device, int realSensorId, Entity entity);

  @Query("SELECT S FROM Entity E JOIN E.sensors S "
      + "WHERE S.sensorId = :sensorId AND E = :entity")
  Sensor findBySensorIdAndEntities(int sensorId, Entity entity);

  @Query("SELECT S FROM Sensor S JOIN S.device D JOIN D.gateway G "
      + "WHERE G.gatewayId = :gatewayId AND D.realDeviceId = :realDeviceId "
      + "AND S.realSensorId = :realSensorId")
  Sensor findByGatewayIdAndRealDeviceIdAndRealSensorId(int gatewayId, int realDeviceId,
                                                       int realSensorId);
}