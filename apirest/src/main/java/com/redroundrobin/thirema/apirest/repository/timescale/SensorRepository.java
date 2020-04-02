package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "timescaleSensorRepository")
public interface SensorRepository extends CrudRepository<Sensor, Timestamp> {

  Iterable<Sensor> findAllByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int gatewayId,
                                                                           int deviceId,
                                                                           int sensorId);

  @Query(value = "SELECT * FROM sensors WHERE gateway_id = :gatewayId AND device_id = :deviceId "
      + "AND sensor_id = :sensorId ORDER BY time DESC LIMIT :resultsNumber",
      nativeQuery = true)
  Iterable<Sensor> findTopNByGatewayIdAndDeviceIdAndSensorId(int resultsNumber, int gatewayId,
                                                              int deviceId, int sensorId);

  Sensor findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int gatewayId, int deviceId,
                                                                int sensorId);
}
