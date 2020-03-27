package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "sensor_id")
  private int sensorId;
  private String type;

  @Column(name = "real_sensor_id")
  private int realSensorId;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "device_id")
  private Device device;

  @JsonBackReference
  @OneToMany(mappedBy = "sensor")
  private List<Alert> alerts;

  @JsonBackReference
  @ManyToMany
  private List<Entity> entities;


  public Sensor() {
  }

  /**
   * Create the Sensor with the @sensorId, the @type and the @realSensorId.
   *
   * @param sensorId Database sensor id
   * @param type Type of sensor
   * @param realSensorId Sensor Id for the device
   */
  public Sensor(int sensorId, String type, int realSensorId) {
    this.sensorId = sensorId;
    this.type = type;
    this.realSensorId = realSensorId;
  }

  public int getSensorId() {
    return sensorId;
  }

  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getRealSensorId() {
    return realSensorId;
  }

  public void setRealSensorId(int realSensorId) {
    this.realSensorId = realSensorId;
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

  public List<Entity> getEntities() {
    return entities;
  }

  public void setEntities(List<Entity> entities) {
    this.entities = entities;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.sensorId;
    hash = 79 * hash + Objects.hashCode(this.type);
    hash = 79 * hash + this.realSensorId;
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
    if (this.sensorId != other.sensorId) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (this.realSensorId != other.realSensorId) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Device{");
    sb.append("id=").append(sensorId);
    sb.append(", type='").append(type).append("'");
    sb.append(", real_sensor_id=").append(realSensorId);
    sb.append('}');
    return sb.toString();
  }
}
