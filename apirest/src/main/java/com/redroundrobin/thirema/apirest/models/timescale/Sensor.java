package com.redroundrobin.thirema.apirest.models.timescale;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Timestamp time;

  @Column(name = "gateway_name", nullable = false)
  private String gatewayName;

  @Column(name = "real_device_id", nullable = false)
  private int realDeviceId;

  @Column(name = "real_sensor_id", nullable = false)
  private int realSensorId;

  private double value;

  public Sensor() {
  }

  public Sensor(String gatewayName, int realDeviceId, int realSensorId) {
    this.time = Timestamp.from(Instant.now());
    this.gatewayName = gatewayName;
    this.realDeviceId = realDeviceId;
    this.realSensorId = realSensorId;
  }

  public Timestamp getTime() {
    return time;
  }

  public String getGatewayName() {
    return gatewayName;
  }

  public void setGatewayName(String gatewayName) {
    this.gatewayName = gatewayName;
  }

  public int getRealDeviceId() {
    return realDeviceId;
  }

  public void setRealDeviceId(int realDeviceId) {
    this.realDeviceId = realDeviceId;
  }

  public int getRealSensorId() {
    return realSensorId;
  }

  public void setRealSensorId(int realSensorId) {
    this.realSensorId = realSensorId;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
