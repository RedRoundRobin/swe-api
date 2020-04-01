package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "users")
public class User {

  public enum Role {
    USER, MOD, ADMIN;

    @JsonValue
    public int toValue() {
      return ordinal();
    }
  }

  @Id
  @GeneratedValue(generator = "user_user_id_seq", strategy = GenerationType.SEQUENCE)
  @SequenceGenerator(
      name = "users_user_id_seq",
      sequenceName = "users_user_id_seq",
      allocationSize = 50
  )
  @Column(name = "user_id")
  private int userId;
  private String name;
  private String surname;
  private String email;
  private String password;

  @Enumerated(EnumType.ORDINAL)
  private Role type;

  @Column(name = "telegram_name")
  private String telegramName;

  @Column(name = "telegram_chat")
  private String telegramChat;

  @Column(name = "two_factor_authentication")
  private boolean tfa;
  private boolean deleted;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "entity_id")
  private Entity entity;

  @JsonManagedReference
  @OneToMany(mappedBy = "user")
  private List<View> views;

  @JsonManagedReference
  @ManyToMany
  @JoinTable(
      name = "disabled_users_alerts",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "alert_id"))
  private List<Alert> disabledAlerts;

  public void setId(int userId) {
    this.userId = userId;
  }

  @JsonProperty(value = "userId")
  public int getId() {
    return userId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getSurname() {
    return surname;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void setType(Role type) {
    this.type = type;
  }

  public Role getType() {
    return type;
  }

  public void setTelegramName(String telegramName) {
    this.telegramName = telegramName;
  }

  public String getTelegramName() {
    return telegramName;
  }

  public void setTelegramChat(String telegramChat) {
    this.telegramChat = telegramChat;
  }

  public String getTelegramChat() {
    return telegramChat;
  }

  public void setTfa(boolean tfa) {
    this.tfa = tfa;
  }

  public boolean getTfa() {
    return this.tfa;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public boolean isDeleted() {
    return this.deleted;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return entity;
  }

  public List<Alert> getDisabledAlerts() {
    return disabledAlerts;
  }

  public void setDisabledAlerts(List<Alert> disabledAlerts) {
    this.disabledAlerts = disabledAlerts;
  }

  public List<View> getViews() {
    return views;
  }

  public void setViews(List<View> views) {
    this.views = views;
  }
}
