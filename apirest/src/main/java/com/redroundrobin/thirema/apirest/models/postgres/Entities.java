package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "entities")
public class Entities {
    @Id
    private int entity_id;

    private double threshold;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 32)
    private String location;

    @Column(nullable = false)
    private boolean deleted = false;


    public int getEntity_id() {
        return entity_id;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isDeleted() {
        return deleted;
    }


    public void setEntity_id(int entity_id) {
        this.entity_id = entity_id;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
