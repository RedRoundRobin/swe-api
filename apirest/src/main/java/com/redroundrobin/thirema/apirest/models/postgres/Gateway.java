package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "gateways")
public class Gateway implements Serializable {
  @Id
  @GeneratedValue(generator = "gateways_gateway_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "gateways_gateway_id_seq",
      sequenceName = "gateways_gateway_id_seq",
      allocationSize = 50
  )
  @Column(name = "gateway_id")
  private int gatewayId;
  private String name;

  @Column(name = "last_sent")
  private Timestamp lastSent;

  public Gateway() {
    // default constructor
  }

  public Gateway(String name) {
    this.name = name;
  }

  public Gateway(int gatewayId, String name) {
    this.gatewayId = gatewayId;
    this.name = name;
  }

  @JsonProperty(value = "gatewayId")
  public int getId() {
    return gatewayId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getLastSent() {
    return lastSent;
  }

  public void setLastSent(Timestamp lastSent) {
    this.lastSent = lastSent;
  }
}
