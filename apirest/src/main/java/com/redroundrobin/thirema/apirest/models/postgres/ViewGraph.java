package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
