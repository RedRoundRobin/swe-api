package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "entities")
public class Entity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "entity_id")
  private int entityId;
  private String name;
  private String location;
  private boolean deleted;

  @JsonManagedReference
  @OneToMany(mappedBy = "entity")
  private List<User> users;

  @JsonManagedReference
  @OneToMany(mappedBy = "entity")
  private List<Alert> alerts;

  @JsonManagedReference
  @ManyToMany
  @JoinTable(
      name = "entity_sensors",
      joinColumns = @JoinColumn(name = "entity_id"),
      inverseJoinColumns = @JoinColumn(name = "sensor_id"))
  private List<Sensor> sensors;

  public Entity() {
  }

  public int getId() {
    return entityId;
  }

  public void setId(int entityId) {
    this.entityId = entityId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public List<Alert> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<Alert> alerts) {
    this.alerts = alerts;
  }

  public boolean isDeleted() {
    return this.deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public List<Sensor> getSensors() {
    return sensors;
  }

  public void setSensors(List<Sensor> sensors) {
    this.sensors = sensors;
  }
}