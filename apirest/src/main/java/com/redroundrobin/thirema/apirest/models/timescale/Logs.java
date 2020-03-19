package com.redroundrobin.thirema.apirest.models.timescale;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "logs")
public class Logs {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Timestamp time;

  @Column(nullable = false)
  private int log_id;


  public Timestamp getTime() {
    return time;
  }

  public int getLog_id() {
    return log_id;
  }
}
