package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "sensors")
public class Sensors {
    @Id
    private int sensor_id;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false)
    private int device_sensor_id;

    private int device_id;


    public int getSensor_id() {
        return sensor_id;
    }

    public String getType() {
        return type;
    }

    public int getDevice_sensor_id() {
        return device_sensor_id;
    }

    public int getDevice_id() {
        return device_id;
    }


    public void setSensor_id(int sensor_id) {
        this.sensor_id = sensor_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDevice_sensor_id(int device_sensor_id) {
        this.device_sensor_id = device_sensor_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }
}
