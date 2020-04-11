package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "views")
public class View implements Serializable {
  @Id
  @GeneratedValue(generator = "views_view_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "views_view_id_seq",
      sequenceName = "views_view_id_seq",
      allocationSize = 25
  )
  @Column(name = "view_id")
  private int viewId;
  private String name;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
  @JsonIdentityReference(alwaysAsId = true)
  private User user;

  public View() {
    // default constructor
  }

  public View(String name, User user) {
    this.name = name;
    this.user = user;
  }

  public View(int viewId, String name, User user) {
    this.viewId = viewId;
    this.name = name;
    this.user = user;
  }

  //setter and getter

  @JsonProperty(value = "viewId")
  public int getId() {
    return viewId;
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
}
