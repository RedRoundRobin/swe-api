package com.redroundrobin.thirema.apirest.models.postgres;

import java.io.Serializable;

public class Disabled_users_alertsPK implements Serializable {
    private int user_id;

    private int alert_id;

    // default constructor

    public Disabled_users_alertsPK(int user_id, int alert_id) {
        this.user_id = user_id;
        this.alert_id = alert_id;
    }

    // equals() and hashCode()
}