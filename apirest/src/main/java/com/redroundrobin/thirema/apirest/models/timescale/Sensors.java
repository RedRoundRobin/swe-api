package com.redroundrobin.thirema.apirest.models.timescale;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensors {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Timestamp time;

  @Column(name = "sensor_id", nullable = false)
  private int sensorId;

  @Column(name = "device_id", nullable = false)
  private int deviceId;

  private double value;


  public Timestamp getTime() {
    return time;
  }

  public int getSensorId() {
    return sensorId;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public double getValue() {
    return value;
  }

}
