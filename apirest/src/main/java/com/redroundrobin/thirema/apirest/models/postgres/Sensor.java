package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensor {

  @Id
  @GeneratedValue(generator = "sensors_sensor_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "sensors_sensor_id_seq",
      sequenceName = "sensors_sensor_id_seq",
      allocationSize = 50
  )
  @Column(name = "sensor_id")
  private int sensorId;
  private String type;

  @Column(name = "real_sensor_id")
  private int realSensorId;

  @JsonIgnore
  @OneToMany(mappedBy = "sensor")
  private List<Alert> alerts;

  @ManyToOne
  @JoinColumn(name = "device_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "deviceId")
  @JsonIdentityReference(alwaysAsId = true)
  private Device device;

  @JsonIgnore
  @ManyToMany(mappedBy = "sensors")
  private List<Entity> entities;

  @JsonIgnore
  @OneToMany(mappedBy = "sensor1")
  private List<ViewGraph> viewGraphs1;

  @JsonIgnore
  @OneToMany(mappedBy = "sensor2")
  private List<ViewGraph> viewGraphs2;

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

  @JsonProperty(value = "sensorId")
  public int getId() {
    return sensorId;
  }

  public void setId(int sensorId) {
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

  public List<ViewGraph> getViewGraphs1() {
    return viewGraphs1;
  }

  public void setViewGraphs1(List<ViewGraph> viewGraphs1) {
    this.viewGraphs1 = viewGraphs1;
  }

  public List<ViewGraph> getViewGraphs2() {
    return viewGraphs2;
  }

  public void setViewGraphs2(List<ViewGraph> viewGraphs2) {
    this.viewGraphs2 = viewGraphs2;
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
