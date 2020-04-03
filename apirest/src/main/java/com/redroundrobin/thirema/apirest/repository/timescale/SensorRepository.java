package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "timescaleSensorRepository")
public interface SensorRepository extends CrudRepository<Sensor, Timestamp> {

  Iterable<Sensor> findAllByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(
      String gatewayId, int realDeviceId, int realSensorId);

  @Query(value = "SELECT * FROM sensors WHERE gateway_name = :gatewayName "
      + "AND real_device_id = :realDeviceId AND real_sensor_id = :realSensorId "
      + "ORDER BY time DESC LIMIT :resultsNumber",
      nativeQuery = true)
  Iterable<Sensor> findTopNByGatewayNameAndRealDeviceIdAndRealSensorId(int resultsNumber,
                                                                       String gatewayName,
                                                                       int realDeviceId,
                                                                       int realSensorId);

  Sensor findTopByGatewayNameAndRealDeviceIdAndRealSensorIdOrderByTimeDesc(String gatewayName,
                                                                           int realDeviceId,
                                                                           int realSensorId);
}
