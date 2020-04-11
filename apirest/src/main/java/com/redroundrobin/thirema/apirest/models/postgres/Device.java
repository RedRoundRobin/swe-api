package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
      allocationSize = 25
  )
  @Column(name = "device_id")
  private int deviceId;

  private String name;
  private int frequency;

  @Column(name = "real_device_id")
  private int realDeviceId;

  @ManyToOne
  @JoinColumn(name = "gateway_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "gatewayId")
  @JsonIdentityReference(alwaysAsId = true)
  private Gateway gateway;

  public Device() {
  }

  public Device(String name, int frequency, int realDeviceId) {
    this.name = name;
    this.frequency = frequency;
    this.realDeviceId = realDeviceId;
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
}
