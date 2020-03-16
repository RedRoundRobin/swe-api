package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.util.Objects;
import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int device_id;

    private String name;
    private int frequency;

    @JsonManagedReference
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Sensor> sensors;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "gateway_id")
    private Gateway gateway;

    public Device(){}

    public Device(int deviceId, String name, int frequency, int gatewayId) {
        this.device_id = deviceId;
        this.name = name;
        this.frequency = frequency;
    }

    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int deviceId){
        this.device_id = deviceId;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getFrequency(){
        return this.frequency;
    }

    public void setFrequency(int frequency){
        this.frequency = frequency;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.device_id;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + this.frequency;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Device other = (Device) obj;
        if (this.device_id != other.device_id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.frequency != other.frequency) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Device{");
        sb.append("id=").append(device_id);
        sb.append(", name='").append(name).append("'");
        sb.append(", frequency=").append(frequency);
        sb.append(", sensors=").append(sensors);
        sb.append('}');
        return sb.toString();
    }
}
