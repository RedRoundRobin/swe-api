package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "views")
public class View {
  @Id
  @GeneratedValue(generator = "views_view_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "views_view_id_seq",
      sequenceName = "views_view_id_seq",
      allocationSize = 50
  )
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

  @JsonProperty(value = "viewId")
  public int getId() {
    return viewId;
  }

  public void setId(int viewId) {
    this.viewId = viewId;
  }

  public User getUser() {
    return user;
  }

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
