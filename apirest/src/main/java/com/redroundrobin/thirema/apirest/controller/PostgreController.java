package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostgreController {

  @Autowired
  private SensorService sensorService;

  @Autowired
  private GatewayService gatewayService;

  @Autowired
  private JwtUtil jwtTokenUtil;





  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////DEBUG///////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////



  //tutti i sensori
  @GetMapping(value = {"/sensors"})
  public List<Sensor> sensors() {
    return sensorService.findAll();
  }
}

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
