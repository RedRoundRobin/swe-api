package com.redroundrobin.thirema.apirest.models.timescale;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "conditions")
public class Conditions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private Timestamp time;

    @Column(nullable = false)
    private String location;

    private double temperature;

    private double humidity;


    public Conditions(Timestamp time, String location, double temperature, double humidity) {
        this.time = time;
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
    }


    public long getId() { return id; }

    public Timestamp getTime() { return time; }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }
}
