package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "views_graphs")
public class ViewGraph {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int graphId;
  private int correlation;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "view_id")
  private View view;

}
