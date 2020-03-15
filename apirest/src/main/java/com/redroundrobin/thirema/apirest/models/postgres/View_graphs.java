package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "view_graphs")
public class View_graphs {
    @Id
    private int graph_id;

    @Column(nullable = false)
    private short correlation;

    private int view_id;
    private int sensors_1_id;
    private int sensors_2_id;


    public int getGraph_id() {
        return graph_id;
    }

    public short getCorrelation() {
        return correlation;
    }

    public int getView_id() {
        return view_id;
    }

    public int getSensors_1_id() {
        return sensors_1_id;
    }

    public int getSensors_2_id() {
        return sensors_2_id;
    }


    public void setGraph_id(int graph_id) {
        this.graph_id = graph_id;
    }

    public void setCorrelation(short correlation) {
        this.correlation = correlation;
    }

    public void setView_id(int view_id) {
        this.view_id = view_id;
    }

    public void setSensors_1_id(int sensors_1_id) {
        this.sensors_1_id = sensors_1_id;
    }

    public void setSensors_2_id(int sensors_2_id) {
        this.sensors_2_id = sensors_2_id;
    }
}
