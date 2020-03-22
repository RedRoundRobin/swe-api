package com.redroundrobin.thirema.apirest.models.timescale;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "logs")
public class Logs {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Timestamp time;

  @Column(name = "log_id", nullable = false)
  private int logId;


  public Timestamp getTime() {
    return time;
  }

  public int getLogId() {
    return logId;
  }
}
