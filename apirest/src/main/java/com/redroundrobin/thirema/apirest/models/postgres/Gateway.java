package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@javax.persistence.Entity
@Table(name = "gateways")
public class Gateway {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int gateway_id;
  private String name;

  @JsonManagedReference
  @OneToMany(mappedBy = "gateway", cascade = CascadeType.ALL)
  private List<Device> devices;

  public Gateway() {
  }

  public Gateway(int gatewayId, String name) {
    this.gateway_id = gatewayId;
    this.name = name;
  }

  public int getId() {
    return gateway_id;
  }

  public void setId(int gatewayId) {
    this.gateway_id = gatewayId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Device> getDevices() {
    return devices;
  }

  public void setDevices(List<Device> devices) {
    this.devices = devices;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + this.gateway_id;
    hash = 79 * hash + Objects.hashCode(this.name);
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
    final Gateway other = (Gateway) obj;
    if (this.gateway_id != other.gateway_id) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Device{");
    sb.append("id=").append(gateway_id);
    sb.append(", name='").append(name).append("'");
    sb.append(", devices=").append(devices);
    sb.append('}');
    return sb.toString();
  }

}
