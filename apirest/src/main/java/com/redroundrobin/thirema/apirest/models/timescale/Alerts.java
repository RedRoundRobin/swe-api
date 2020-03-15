package com.redroundrobin.thirema.apirest.models.timescale;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "alerts")
public class Alerts {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Timestamp time;

    @Column(nullable = false)
    private int sensor_id;

    @Column(nullable = false)
    private int device_id;

    private double value;


    public Timestamp getTime() {
        return time;
    }

    public int getSensor_id() {
        return sensor_id;
    }

    public int getDevice_id() {
        return device_id;
    }

    public double getValue() {
        return value;
    }

}
