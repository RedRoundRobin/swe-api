package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "views")
public class Views {
    @Id
    private int view_id;

    @Column(nullable = false, length = 32)
    private String name;


    public int getView_id() {
        return view_id;
    }

    public String getName() {
        return name;
    }


    public void setView_id(int view_id) {
        this.view_id = view_id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
