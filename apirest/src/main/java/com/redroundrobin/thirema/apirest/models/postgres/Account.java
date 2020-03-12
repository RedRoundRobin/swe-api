package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "account")
public class Account {

    @Id
    private Long user_id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private Timestamp created_on;

    private Timestamp last_login;


    public Long getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getCreated_on() {
        return created_on;
    }

    public Timestamp getLast_login() {
        return last_login;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}