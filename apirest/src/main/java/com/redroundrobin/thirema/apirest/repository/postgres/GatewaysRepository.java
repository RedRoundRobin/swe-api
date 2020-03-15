package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.Gateways;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewaysRepository extends JpaRepository<Gateways, Integer> {}
