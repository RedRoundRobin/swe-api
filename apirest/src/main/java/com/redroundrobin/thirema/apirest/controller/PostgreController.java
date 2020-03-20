package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.*;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class PostgreController {

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private SensorService sensorService;

  @Autowired
  private GatewayService gatewayService;

  @Autowired
  private UserService userService;

  //tutti i gateway
  @GetMapping(value = {"/gateways"})
  public List<Gateway> gateways() {
    return gatewayService.findAll();
  }

  //un determinato gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}"})
  public Gateway gateway(@PathVariable("gatewayid") int gatewayId) {
    return gatewayService.find(gatewayId);
  }

  //tutti i dispositivi
  @GetMapping(value = {"/devices"})
  public List<Device> devices() {
    return deviceService.findAll();
  }

  //un determinato dispositivo
  @GetMapping(value = {"/device/{deviceid:.+}"})
  public Device device(@PathVariable("deviceid") int deviceId) {
    return deviceService.find(deviceId);
  }

  //tutti i sensori di un dispositivo
  @GetMapping(value = {"/device/{deviceid:.+}/sensors"})
  public List<Sensor> deviceSensors(@PathVariable("deviceid") int deviceId) {
    return deviceService.find(deviceId).getSensors();
  }

  //un sensore di un dispositivo
  @GetMapping(value = {"/device/{deviceid:.+}/sensor/{sensorid:.+}"})
  public Sensor sensor(@PathVariable("deviceid") int deviceId, @PathVariable("sensorid") int sensorId) {
    return deviceService.find(deviceId).getSensors().stream().filter(
        sensor -> sensor.getReal_sensor_id() == sensorId
    ).collect(Collectors.toList()).get(0);
  }

  //tutti gli user
  @GetMapping(value = {"/users"})
  public List<User> users() {
    return userService.findAll();
  }

  //un determinato user
  @GetMapping(value = {"/user/{userid:.+}"})
  public User user(@PathVariable("userid") int userId) {
    return userService.find(userId);
  }

  // TODO: 13/03/20 il resto delle api per quanto riguarda, guardare jwt per auth


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////DEBUG///////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  //tutti i dispositivi del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/devices"})
  public List<Device> gatewayDevices(@PathVariable("gatewayid") int gatewayid) {
    return gatewayService.find(gatewayid).getDevices();
  }

  //il dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}"})
  public Device gatewayDevice(@PathVariable("gatewayid") int gatewayid, @PathVariable("deviceid") int deviceId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0);
  }

  //tutti i sensori che appartengono al dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}/sensors"})
  public List<Sensor> gatewayDeviceSensors(@PathVariable("gatewayid") int gatewayid, @PathVariable("deviceid") int deviceId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0).getSensors();
  }

  //il sensore che appartiene al dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}/sensor/{sensorid:.+}"})
  public Sensor gatewayDeviceSensor(@PathVariable("gatewayid") int gatewayid, @PathVariable("deviceid") int deviceId, @PathVariable("sensorid") int sensorId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0).getSensors().stream().filter(
        sensor -> sensor.getReal_sensor_id() == sensorId
    ).collect(Collectors.toList()).get(0);
  }

  //tutti i sensori
  @GetMapping(value = {"/sensors"})
  public List<Sensor> sensors() {
    return sensorService.findAll();
  }

  //funzione di test
  @GetMapping(value = {"/test"})
  public void deviceSensors(@RequestBody Map<String, Object> payload) {
    System.out.println(payload);
  }

  //funzione di test per ottenere l'username dal token
  @GetMapping(value = "/credentials")
  public String getCredentials(@RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);

    return jwtTokenUtil.extractUsername(token);
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JwtUtil jwtTokenUtil;

  @Autowired
  private UserRepository userRepository;

  @PostMapping(value = "/authenticate")
  public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
    String email = authenticationRequest.getUsername();
    String password = authenticationRequest.getPassword();

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, password)
      );
    } catch (BadCredentialsException e) {
      throw new Exception("Incorrect username or password", e);
    }

    final UserDetails userDetails = userService
        .loadUserByUsername(email);

    final String jwt = jwtTokenUtil.generateToken("webapp", userDetails);
    final User user = userService.findByEmail(email);

    return ResponseEntity.ok(new AuthenticationResponse(jwt, user));
  }

  //funzione di controllo username Telegram e salvataggio chatID
  @PostMapping(value = {"/auth/telegram"})
  public ResponseEntity<?> checkUser(@RequestBody AuthenticationRequestTelegram authenticationRequest) {
    String telegramName = authenticationRequest.getTelegramName();
    String chatId = authenticationRequest.getTelegramChat();

    int code = 2;
    String token = "";

    if (userService.findByTelegramName(telegramName) == null)
      code = 0;
    if (userService.findByTelegramNameAndTelegramChat(telegramName, chatId) == null) {
      code = 1;

      User user = userService.findByTelegramName(telegramName);
      user.setTelegramChat(chatId);
      user = userRepository.save(user);
    }

    if (code != 0) {
      final UserDetails userDetails = userService
          .loadUserByTelegramName(telegramName);

      token = jwtTokenUtil.generateToken("telegram", userDetails);
    }

    return ResponseEntity.ok(new BaseResponse(new AuthenticationResponseTelegram(code, token)));
  }
}

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
