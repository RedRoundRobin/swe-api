package com.redroundrobin.thirema.apirest.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

@javax.persistence.Entity
@Table(name = "views")
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int viewId;
    private String name;

    @JsonManagedReference
    @OneToMany(mappedBy = "view")
    private List<ViewGraph> viewGraphs;

    //setter and getter
}
