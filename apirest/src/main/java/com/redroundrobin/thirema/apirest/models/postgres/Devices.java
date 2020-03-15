package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "devices")
public class Devices {
    @Id
    private int device_id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false)
    private int frequency;

    private int gateway_id;


    public int getDevice_id() {
        return device_id;
    }

    public String getName() {
        return name;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getGateway_id() {
        return gateway_id;
    }


    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setGateway_id(int gateway_id) {
        this.gateway_id = gateway_id;
    }
}
