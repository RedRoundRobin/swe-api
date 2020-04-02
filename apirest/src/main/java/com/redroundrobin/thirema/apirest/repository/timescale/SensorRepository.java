package com.redroundrobin.thirema.apirest.repository.timescale;

import java.sql.Timestamp;
import java.util.List;
import com.redroundrobin.thirema.apirest.models.timescale.Sensor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "timescaleSensorRepository")
public interface SensorRepository extends CrudRepository<Sensor, Timestamp> {

  Iterable<Sensor> findAllByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int gatewayId, int deviceId,
                                                            int sensorId);

  @Query(value = "SELECT * FROM Alert WHERE gatewayId = :gatewayId AND deviceId = :deviceId "
      + "AND sensorId = :sensorId ORDER BY time DESC LIMIT :resultsNumber ORDER BY time DESC",
      nativeQuery = true)
  Iterable<Sensor> findTopNByGatewayIdAndDeviceIdAndSensorId(int resultsNumber, int gatewayId,
                                                              int deviceId, int sensorId);

  Sensor findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int gatewayId, int deviceId,
                                                                int sensorId);
}
