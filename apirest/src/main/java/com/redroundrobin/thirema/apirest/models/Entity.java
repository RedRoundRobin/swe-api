package com.redroundrobin.thirema.apirest.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "entities")
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int entityId;
    private String name;
    private String location;
    private boolean deleted;

    @JsonManagedReference
    @OneToOne
    @JoinColumn(name = "entityId")
    User user;

    public Entity() {}

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}