package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class Users {
    @Id
    private int user_id;

    private Integer entity_id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = false, length = 32)
    private String surname;

    @Column(nullable = false, length = 32)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private short type;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(length = 32)
    private String telegram_name;

    @Column(length = 32)
    private String telegram_chat;

    private boolean two_factor_authentication = false;


    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public short getType() {
        return type;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getTelegram_name() {
        return telegram_name;
    }

    public String getTelegram_chat() {
        return telegram_chat;
    }

    public boolean isTwo_factor_authentication() {
        return two_factor_authentication;
    }

    public int getEntity_id() {
        return entity_id;
    }


    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(short type) {
        this.type = type;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setTelegram_name(String telegram_name) {
        this.telegram_name = telegram_name;
    }

    public void setTelegram_chat(String telegram_chat) {
        this.telegram_chat = telegram_chat;
    }

    public void setTwo_factor_authentication(boolean two_factor_authentication) {
        this.two_factor_authentication = two_factor_authentication;
    }

    public void setEntity_id(int entity_id) {
        this.entity_id = entity_id;
    }
}
