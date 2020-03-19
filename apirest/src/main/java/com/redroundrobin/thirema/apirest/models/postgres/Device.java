package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.util.Objects;
import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "devices")
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int device_id;

  private String name;
  private int frequency;
  private int real_device_id;

  @JsonManagedReference
  @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
  private List<Sensor> sensors;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "gateway_id")
  private Gateway gateway;

  public Device() {
  }

  public Device(int deviceId, String name, int frequency, int gatewayId, int real_device_id) {
    this.device_id = deviceId;
    this.name = name;
    this.frequency = frequency;
    this.real_device_id = real_device_id;
  }

  public int getDeviceId() {
    return device_id;
  }

  public void setDeviceId(int deviceId) {
    this.device_id = deviceId;
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

  public int getReal_device_id() {
    return real_device_id;
  }

  public void setReal_device_id(int real_device_id) {
    this.real_device_id = real_device_id;
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Device device = (Device) o;
    return device_id == device.device_id &&
        frequency == device.frequency &&
        real_device_id == device.real_device_id &&
        Objects.equals(name, device.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.device_id;
    hash = 79 * hash + Objects.hashCode(this.name);
    hash = 79 * hash + this.frequency;
    hash = 79 * hash + this.real_device_id;
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Device{");
    sb.append("id=").append(device_id);
    sb.append(", name='").append(name).append("'");
    sb.append(", frequency=").append(frequency);
    sb.append(", sensors=").append(sensors);
    sb.append(", real_id=").append(real_device_id);
    sb.append('}');
    return sb.toString();
  }
}
