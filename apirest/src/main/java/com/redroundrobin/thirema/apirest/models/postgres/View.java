package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.List;
import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "views")
public class View {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "view_id")
  private int viewId;
  private String name;

  @JsonBackReference //non sono sicuro! (Fouad)
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @JsonManagedReference
  @OneToMany(mappedBy = "view")
  private List<ViewGraph> viewGraphs;

  //setter and getter

  public int getViewId() {
    return viewId;
  }

  public void setViewId(int viewId) {
    this.viewId = viewId;
  }

  public User getUser() { return user; }

  public void setUser(User userId) {
    this.user = userId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ViewGraph> getViewGraphs() {
    return viewGraphs;
  }

  public void setViewGraphs(List<ViewGraph> viewGraphs) {
    this.viewGraphs = viewGraphs;
  }
}
