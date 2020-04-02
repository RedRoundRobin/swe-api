package com.redroundrobin.thirema.apirest.models.timescale;

import java.sql.Timestamp;
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

  @Column(name = "gateway_id", nullable = false)
  private int gatewayId;

  @Column(name = "device_id", nullable = false)
  private int deviceId;

  @Column(name = "sensor_id", nullable = false)
  private int sensorId;

  private double value;

  public Timestamp getTime() {
    return time;
  }

  public void setTime(Timestamp time) {
    this.time = time;
  }

  public int getGatewayId() {
    return gatewayId;
  }

  public void setGatewayId(int gatewayId) {
    this.gatewayId = gatewayId;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }

  public int getSensorId() {
    return sensorId;
  }

  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
