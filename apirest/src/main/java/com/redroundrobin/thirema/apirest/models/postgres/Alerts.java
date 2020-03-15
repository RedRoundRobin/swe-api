package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "alerts")
public class Alerts {
    @Id
    private int alert_id;

    private double threshold;

    @Column(nullable = false)
    private short type;

    @Column(nullable = false)
    private boolean deleted = false;

    private int sensor_id;
    private int entity_id;


    public int getAlert_id() {
        return alert_id;
    }

    public double getThreshold() {
        return threshold;
    }

    public short getType() {
        return type;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getSensor_id() {
        return sensor_id;
    }

    public int getEntity_id() {
        return entity_id;
    }


    public void setAlert_id(int alert_id) {
        this.alert_id = alert_id;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setType(short type) {
        this.type = type;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setSensor_id(int sensor_id) {
        this.sensor_id = sensor_id;
    }

    public void setEntity_id(int entity_id) {
        this.entity_id = entity_id;
    }
}
