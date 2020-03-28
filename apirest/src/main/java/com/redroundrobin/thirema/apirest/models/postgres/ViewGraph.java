package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

@javax.persistence.Entity
@Table(name = "views_graphs")
public class ViewGraph {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "graph_id")
  private int graphId;
  private int correlation;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "view_id")
  private View view;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "sensor_1_id")
  private Sensor sensor1;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "sensor_2_id")
  private Sensor sensor2;

  public int getId() {
    return graphId;
  }

  public void setId(int graphId) {
    this.graphId = graphId;
  }

  public int getCorrelation() {
    return correlation;
  }

  public void setCorrelation(int correlation) {
    this.correlation = correlation;
  }

  public View getView() {
    return view;
  }

  public void setView(View view) {
    this.view = view;
  }

  public Sensor getSensor1() {
    return sensor1;
  }

  public void setSensor1(Sensor sensor1) {
    this.sensor1 = sensor1;
  }

  public Sensor getSensor2() {
    return sensor2;
  }

  public void setSensor2(Sensor sensor2) {
    this.sensor2 = sensor2;
  }
}
