package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Disabled_users_alerts;
import com.redroundrobin.thirema.apirest.models.postgres.Disabled_users_alertsPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Disabled_users_alertsRepository extends JpaRepository<Disabled_users_alerts, Disabled_users_alertsPK> { }
