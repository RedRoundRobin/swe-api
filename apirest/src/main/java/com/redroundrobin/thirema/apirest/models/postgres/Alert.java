package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "alerts")
public class Alert {
  @Id
  @GeneratedValue(generator = "alerts_alert_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "alerts_alert_id_seq",
      sequenceName = "alerts_alert_id_seq",
      allocationSize = 50
  )
  @Column(name = "alert_id")
  private int alertId;
  private double threshold;
  private int type;
  private boolean deleted;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "entity_id")
  private Entity entity;

  @JsonManagedReference
  @ManyToOne
  @JoinColumn(name = "sensor_id")
  private Sensor sensor;

  @JsonBackReference
  @ManyToMany(mappedBy = "disabledAlerts")
  private List<User> users;


  public void setAlertId(int alertId) {
    this.alertId = alertId;
  }

  @JsonProperty(value = "alertId")
  public int getAlertId() {
    return alertId;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean getDeleted() {
    return this.deleted;
  }

  public void setSensor(Sensor sensor) {
    this.sensor = sensor;
  }

  public Sensor getSensor() {
    return sensor;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return entity;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }
}
