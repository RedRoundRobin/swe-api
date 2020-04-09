package com.redroundrobin.thirema.apirest.models.timescale;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "logs")
public class Log implements Serializable {

  @Id
  private Timestamp time;

  @Column(name = "user_id", nullable = false)
  private int userId;

  @Column(name = "ip_addr")
  private String ipAddr;
  private String operation;
  private String data;

  public Log() {

  }

  public Log(int userId, String ipAddr, String operation, String data) {
    time = Timestamp.from(Instant.now());
    this.userId = userId;
    this.ipAddr = ipAddr;
    this.operation = operation;
    this.data = data;
  }

  public Timestamp getTime() {
    return time;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getIpAddr() {
    return ipAddr;
  }

  public void setIpAddr(String ip) {
    this.ipAddr = ip;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
