package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int sensor_id;
  private String type;
  private int real_sensor_id;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "device_id")
  private Device device;

  @JsonBackReference
  @OneToMany(mappedBy = "sensor")
  private List<Alert> alerts;


  public Sensor() {
  }

  public Sensor(int SensorId, String type, int real_sensor_id, int device_id) {
    this.sensor_id = SensorId;
    this.type = type;
    this.real_sensor_id = real_sensor_id;
  }

  public int getSensorId() {
    return sensor_id;
  }

  public void setSensorId(int sensorId) {
    this.sensor_id = sensorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getReal_sensor_id() {
    return real_sensor_id;
  }

  public void setReal_sensor_id(int real_sensor_id) {
    this.real_sensor_id = real_sensor_id;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public List<Alert> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<Alert> alerts) {
    this.alerts = alerts;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.sensor_id;
    hash = 79 * hash + Objects.hashCode(this.type);
    hash = 79 * hash + this.real_sensor_id;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Sensor other = (Sensor) obj;
    if (this.sensor_id != other.sensor_id) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (this.real_sensor_id != other.real_sensor_id) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Device{");
    sb.append("id=").append(sensor_id);
    sb.append(", type='").append(type).append("'");
    sb.append(", real_sensor_id=").append(real_sensor_id);
    sb.append('}');
    return sb.toString();
  }
}
