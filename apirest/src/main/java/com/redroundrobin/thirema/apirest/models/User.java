package com.redroundrobin.thirema.apirest.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@javax.persistence.Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userId;
    private String name;
    private String surname;
    private String email;
    private String password;
    private int type;
    private String telegramName;
    private String telegramChat;
    private boolean TFA;
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

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
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

    public void setTFA(boolean TFA) {
        this.TFA = TFA;
    }

    public boolean getTFA() {
        return this.TFA;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
