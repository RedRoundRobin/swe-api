package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Alert;
import com.redroundrobin.thirema.apirest.repository.timescale.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

  private AlertRepository repo;

  @Autowired
  public AlertService(AlertRepository repo) {
    this.repo = repo;
  }

  public List<Alert> findAll() {
    return (List<Alert>) repo.findAll();
  }

  public List<Alert> findAllByGatewayIdAndDeviceIdAndSensorId(int gatewayId, int deviceId,
                                                              int sensorId) {
    return (List<Alert>) repo.findAllByGatewayIdAndDeviceIdAndSensorId(gatewayId, deviceId,
        sensorId);
  }

  public List<Alert> findTopNByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(int resultsNumber,
                                                                              int gatewayId,
                                                                              int deviceId,
                                                                              int sensorId) {
    return (List<Alert>) repo.findTopNByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(
        resultsNumber, gatewayId, deviceId, sensorId);
  }

  public Alert findLastOneByGatewayIdAndDeviceIdAndSensorId(int gatewayId, int deviceId,
                                                            int sensorId) {
    return repo.findTopByGatewayIdAndDeviceIdAndSensorIdOrderByTimeDesc(gatewayId, deviceId,
        sensorId);
  }
}
