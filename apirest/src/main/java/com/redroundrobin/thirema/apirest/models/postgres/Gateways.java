package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "gateways")
public class Gateways {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int gateway_id;

    @Column(nullable = false, length = 32)
    private String name;


    public int getGateway_id() {
        return gateway_id;
    }

    public String getName() {
        return name;
    }


    public void setGateway_id(int gateway_id) {
        this.gateway_id = gateway_id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
