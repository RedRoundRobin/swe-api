package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@javax.persistence.Entity
@Table(name = "alerts")
public class Alert implements Serializable {

  public enum Type {
    GREATER, LOWER, EQUAL;

    @JsonValue
    public int toValue() {
      return ordinal();
    }

    public static boolean isValid(int type) {
      for (int i = 0; i < ViewGraph.Correlation.values().length; ++i) {
        if (type == i) {
          return true;
        }
      }
      return false;
    }
  }

  @Id
  @GeneratedValue(generator = "alerts_alert_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "alerts_alert_id_seq",
      sequenceName = "alerts_alert_id_seq",
      allocationSize = 25
  )
  @Column(name = "alert_id")
  private int alertId;
  private double threshold;
  private Type type;
  private boolean deleted;

  @ManyToOne
  @JoinColumn(name = "entity_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "entityId")
  @JsonIdentityReference(alwaysAsId = true)
  private Entity entity;

  @ManyToOne
  @JoinColumn(name = "sensor_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "sensorId")
  @JsonIdentityReference(alwaysAsId = true)
  private Sensor sensor;

  @Column(name = "last_sent")
  private Timestamp lastSent;

  public Alert() {
    // default constructor
  }

  public Alert(int alertId, double threshold, Type type, Entity entity, Sensor sensor) {
    this.alertId = alertId;
    this.threshold = threshold;
    this.type = type;
    this.entity = entity;
    this.sensor = sensor;
  }

  public Alert(double threshold, Type type, Entity entity, Sensor sensor) {
    this.threshold = threshold;
    this.type = type;
    this.entity = entity;
    this.sensor = sensor;
  }

  @JsonProperty(value = "alertId")
  public int getId() {
    return alertId;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean isDeleted() {
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

  public Timestamp getLastSent() {
    return lastSent;
  }

  public void setLastSent(Timestamp lastSent) {
    this.lastSent = lastSent;
  }
}
