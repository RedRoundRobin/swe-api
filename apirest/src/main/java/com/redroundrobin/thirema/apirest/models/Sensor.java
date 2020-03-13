package com.redroundrobin.thirema.apirest.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Objects;

@javax.persistence.Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int SensorId;
    private String type;
    private int device_sensor_id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    public Sensor(){}

    public Sensor(int SensorId, String type, int device_sensor_id, int device_id) {
        this.SensorId = SensorId;
        this.type = type;
        this.device_sensor_id = device_sensor_id;
    }

    public int getSensorId() {
        return SensorId;
    }

    public void setSensorId(int sensorId) {
        this.SensorId = sensorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDevice_sensor_id() {
        return device_sensor_id;
    }

    public void setDevice_sensor_id(int device_sensor_id) {
        this.device_sensor_id = device_sensor_id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.SensorId;
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + this.device_sensor_id;
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
        final Sensor other = (Sensor) obj;
        if (this.SensorId != other.SensorId) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (this.device_sensor_id != other.device_sensor_id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Device{");
        sb.append("id=").append(SensorId);
        sb.append(", type='").append(type).append("'");
        sb.append(", device_sensor_id=").append(device_sensor_id);
        sb.append('}');
        return sb.toString();
    }
}
