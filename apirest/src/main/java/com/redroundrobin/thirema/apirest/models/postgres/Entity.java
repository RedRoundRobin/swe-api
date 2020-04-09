package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "entities")
public class Entity implements Serializable {
  @Id
  @GeneratedValue(generator = "entities_entity_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "entities_entity_id_seq",
      sequenceName = "entities_entity_id_seq",
      allocationSize = 50
  )
  @Column(name = "entity_id")
  private int entityId;
  private String name;
  private String location;
  private boolean deleted = false;

  @JsonIgnore
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "entity_sensors",
      joinColumns = @JoinColumn(name = "entity_id"),
      inverseJoinColumns = @JoinColumn(name = "sensor_id"))
  private Set<Sensor> sensors;

  public Entity() {
    // default constructor
  }

  public Entity(String name, String location) {
    this.name = name;
    this.location = location;
  }

  public Entity(int entityId, String name, String location) {
    this.entityId = entityId;
    this.name = name;
    this.location = location;
  }

  @JsonProperty(value = "entityId")
  public int getId() {
    return entityId;
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

  public boolean isDeleted() {
    return this.deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Set<Sensor> getSensors() {
    return sensors;
  }

  public void setSensors(Set<Sensor> sensors) {
    this.sensors = sensors;
  }
}