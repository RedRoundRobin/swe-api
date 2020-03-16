package com.redroundrobin.thirema.apirest.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "views")
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int viewId;
    private String name;

    @JsonManagedReference
    @OneToMany(mappedBy = "view")
    @JoinColumn(name = "view_id")
    private ViewGraph viewGraph;

    //setter and getter
}
