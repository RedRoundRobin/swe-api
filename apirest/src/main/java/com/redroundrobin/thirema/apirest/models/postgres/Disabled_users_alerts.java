package com.redroundrobin.thirema.apirest.models.postgres;

import javax.persistence.*;

@Entity
@Table(name = "disabled_users_alerts")
@IdClass(Disabled_users_alertsPK.class)
public class Disabled_users_alerts {
    @Id
    private int user_id;

    @Id
    private int alert_id;


    public int getUser_id() {
        return user_id;
    }

    public int getAlert_id() {
        return alert_id;
    }


    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setAlert_id(int alert_id) {
        this.alert_id = alert_id;
    }
}
