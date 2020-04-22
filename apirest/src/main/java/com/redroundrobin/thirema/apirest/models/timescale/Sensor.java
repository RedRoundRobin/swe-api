package com.redroundrobin.thirema.apirest.models.timescale;

import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

  @Column(name = "req_time", nullable = false)
  private Timestamp reqTime;

  private double value;

  public Sensor() {
  }

  public Sensor(String gatewayName, int realDeviceId, int realSensorId) {
    this.reqTime = this.time = Timestamp.from(Instant.now());
    this.gatewayName = gatewayName;
    this.realDeviceId = realDeviceId;
    this.realSensorId = realSensorId;
  }

  public Timestamp getTime() {
    return reqTime;
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
