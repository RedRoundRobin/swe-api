package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.FetchType;
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
public class Sensor implements Serializable {

  @Id
  @GeneratedValue(generator = "sensors_sensor_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "sensors_sensor_id_seq",
      sequenceName = "sensors_sensor_id_seq",
      allocationSize = 25
  )
  @Column(name = "sensor_id")
  private int sensorId;
  private String type;

  @Column(name = "real_sensor_id")
  private int realSensorId;

  @ManyToOne
  @JoinColumn(name = "device_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "deviceId")
  @JsonIdentityReference(alwaysAsId = true)
  private Device device;

  public Sensor() {
    // default constructor
  }

  /**
   * Create the Sensor with the @sensorId, the @type and the @realSensorId.
   *
   * @param type Type of sensor
   * @param realSensorId Sensor Id for the device
   */
  public Sensor(String type, int realSensorId) {
    this.type = type;
    this.realSensorId = realSensorId;
  }

  public Sensor(int sensorId, String type, int realSensorId) {
    this.sensorId = sensorId;
    this.type = type;
    this.realSensorId = realSensorId;
  }

  @JsonProperty(value = "sensorId")
  public int getId() {
    return sensorId;
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
}
