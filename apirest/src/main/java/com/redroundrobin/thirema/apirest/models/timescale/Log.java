package com.redroundrobin.thirema.apirest.models.timescale;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "logs")
public class Log {

  @Id
  private Timestamp time;

  @Column(name = "user_id", nullable = false)
  private int userId;

  @Column(name = "ip_addr")
  private String ipAddr;

  private String operation;

  private String data;

  public Timestamp getTime() {
    return time;
  }

  public void setTime(Timestamp time) {
    this.time = time;
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
