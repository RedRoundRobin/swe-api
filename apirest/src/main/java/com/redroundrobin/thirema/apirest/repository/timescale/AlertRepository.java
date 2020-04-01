package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Alert;
import java.sql.Timestamp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AlertRepository extends CrudRepository<Alert, Timestamp> {

  Iterable<Alert> findAllByGatewayIdAndDeviceIdAndSensorId(int gatewayId, int deviceId,
                                                           int sensorId);

  @Query(value = "SELECT * FROM Alert WHERE gatewayId = :gatewayId AND deviceId = :deviceId "
      + "AND sensorId = :sensorId ORDER BY time DESC LIMIT :resultNumber", nativeQuery = true)
  Iterable<Alert> findTopNByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int resultsNumber,
                                                                           int gatewayId,
                                                                           int deviceId,
                                                                           int sensorId);

  Alert findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int gatewayId, int deviceId,
                                                                int sensorId);
}
