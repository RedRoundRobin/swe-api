package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.redroundrobin.thirema.apirest.utils.SerializeUser;

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

  @Autowired
  private EntityService entityService;

  @Autowired
  private JwtUtil jwtTokenUtil;

  @Autowired
  private SerializeUser serializeNewUser;

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
  public Sensor sensor(@PathVariable("deviceid") int deviceId,
                       @PathVariable("sensorid") int sensorId) {
    return deviceService.find(deviceId).getSensors().stream()
        .filter(sensor -> sensor.getRealSensorId() == sensorId)
        .collect(Collectors.toList())
        .get(0);
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

  //////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////DEBUG///////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////////////

  //tutti i dispositivi del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/devices"})
  public List<Device> gatewayDevices(@PathVariable("gatewayid") int gatewayid) {
    return gatewayService.find(gatewayid).getDevices();
  }

  //il dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}"})
  public Device gatewayDevice(@PathVariable("gatewayid") int gatewayid,
                              @PathVariable("deviceid") int deviceId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0);
  }

  //tutti i sensori che appartengono al dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}/sensors"})
  public List<Sensor> gatewayDeviceSensors(@PathVariable("gatewayid") int gatewayid,
                                           @PathVariable("deviceid") int deviceId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0).getSensors();
  }

  //il sensore che appartiene al dispositivo del gateway
  @GetMapping(value = {"/gateway/{gatewayid:.+}/device/{deviceid:.+}/sensor/{sensorid:.+}"})
  public Sensor gatewayDeviceSensor(@PathVariable("gatewayid") int gatewayid,
                                    @PathVariable("deviceid") int deviceId,
                                    @PathVariable("sensorid") int sensorId) {
    return gatewayService.find(gatewayid).getDevices().stream().filter(
        device -> device.getDeviceId() == deviceId
    ).collect(Collectors.toList()).get(0).getSensors().stream().filter(
        sensor -> sensor.getRealSensorId() == sensorId
    ).collect(Collectors.toList()).get(0);
  }

  //tutti i sensori
  @GetMapping(value = {"/sensors"})
  public List<Sensor> sensors() {
    return sensorService.findAll();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  //richiesta fatta da un utente autenticato per vedere i device visibili a un altro utente
  @GetMapping(value = {"/users/{userid:.+}/devices"})
  public ResponseEntity<?> getUserDevices (@RequestHeader("Authorization") String authorization,
                                           @PathVariable("userid") int requiredUserId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    User requiredUser = userService.find(requiredUserId);
    if( requiredUser != null && (user.getUserId() == requiredUserId || user.getType() == 2 ||
        user.getType() == 1 && requiredUser.getType() != 2
            && user.getEntity().getEntityId() == requiredUser.getEntity().getEntityId()))
      return ResponseEntity.ok(userService.userDevices(requiredUserId));
    else return new ResponseEntity(HttpStatus.FORBIDDEN);
  }


  //dato un token valid restituisce l'ente di appertenenza o tutti gli enti
  //se il token è di un amministratore
  @GetMapping(value = {"/entities"})
  public ResponseEntity<Object> getUserEntity(
      @RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    if (user.getType() == 2) {
      return ResponseEntity.ok(entityService.findAll());
    } else {
      //utente moderatore || utente membro
      return ResponseEntity.ok(user.getEntity());
    }
  }


  //creazione di un nuovo utente
  @PostMapping(value = {"/users/create"})
  public ResponseEntity<Object> createUser(@RequestHeader("Authorization") String authorization,
                                      @RequestBody String jsonStringUser) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonUser = JsonParser.parseString(jsonStringUser).getAsJsonObject();
    User newUser = serializeNewUser.serializeUser(jsonUser);
    if (user.getType() == 2 || user.getType() == 1
        && user.getEntity().getEntityId() == newUser.getEntity().getEntityId()) {
      return ResponseEntity.ok(userService.save(newUser));
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }


 /*In input prende JsonObject coi field da modificare dello userId*/
  @PutMapping(value = {"/users/{userid:.+}/edit"})
  public ResponseEntity<Object> editUser(@RequestHeader("Authorization") String authorization,
                                      @RequestBody String rawFieldsToEdit, @PathVariable("userid") int userId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject fieldsToEdit = JsonParser.parseString(rawFieldsToEdit).getAsJsonObject();
    if (userService.find(userId) != null && user.getType() == 2 || user.getType() == 1
        && user.getEntity().getEntityId() == userService.find(userId).getEntity().getEntityId()) {
      User editedUser = userService.editUser(userId, fieldsToEdit);
      userService.save(editedUser);
      return ResponseEntity.ok(userService.find(userId));
    }
    return  new ResponseEntity(HttpStatus.FORBIDDEN);
  }
}

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
