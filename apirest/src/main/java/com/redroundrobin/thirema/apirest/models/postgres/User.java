package com.redroundrobin.thirema.apirest.models.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.jsonwebtoken.Claims;

import javax.persistence.*;
import java.util.function.Function;

@javax.persistence.Entity
@Table(name = "users")
public class User {

  public enum Role {
    USER, MOD, ADMIN;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private int userId;
  private String name;
  private String surname;
  private String email;

  @JsonIgnore
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

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getUserId() {
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
}
