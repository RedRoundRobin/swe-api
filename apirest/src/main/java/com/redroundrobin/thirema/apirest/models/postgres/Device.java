package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "devices")
public class Device {

  @Id
  @GeneratedValue(generator = "devices_device_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "devices_device_id_seq",
      sequenceName = "devices_device_id_seq",
      allocationSize = 50
  )
  @Column(name = "device_id")
  private int deviceId;

  private String name;
  private int frequency;

  @Column(name = "real_device_id")
  private int realDeviceId;

  @JsonIgnore
  @OneToMany(mappedBy = "device", cascade = CascadeType.MERGE)
  private List<Sensor> sensors;

  @ManyToOne
  @JoinColumn(name = "gateway_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "gatewayId")
  @JsonIdentityReference(alwaysAsId = true)
  private Gateway gateway;

  public Device() {
  }

  /**
   * Create the device with the @deviceId, @name, @frequency and @realDeviceId.
   *
   * @param deviceId Database device id
   * @param name Name of the device
   * @param frequency Data request frequency
   * @param realDeviceId Device id for the gateway
   */
  public Device(int deviceId, String name, int frequency, int realDeviceId) {
    this.deviceId = deviceId;
    this.name = name;
    this.frequency = frequency;
    this.realDeviceId = realDeviceId;
  }

  @JsonProperty(value = "deviceId")
  public int getId() {
    return deviceId;
  }

  public void setId(int deviceId) {
    this.deviceId = deviceId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getFrequency() {
    return this.frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public int getRealDeviceId() {
    return realDeviceId;
  }

  public void setRealDeviceId(int realDeviceId) {
    this.realDeviceId = realDeviceId;
  }

  public Gateway getGateway() {
    return gateway;
  }

  public void setGateway(Gateway gateway) {
    this.gateway = gateway;
  }

  public List<Sensor> getSensors() {
    return sensors;
  }

  public void setSensors(List<Sensor> sensors) {
    this.sensors = sensors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Device device = (Device) o;
    return deviceId == device.deviceId
        && frequency == device.frequency
        && realDeviceId == device.realDeviceId
        && Objects.equals(name, device.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.deviceId;
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + this.frequency;
    hash = 79 * hash + this.realDeviceId;
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Device{");
    sb.append("id=").append(deviceId);
    sb.append(", name='").append(name).append("'");
    sb.append(", frequency=").append(frequency);
    sb.append(", sensors=").append(sensors);
    sb.append(", real_id=").append(realDeviceId);
    sb.append('}');
    return sb.toString();
  }
}
