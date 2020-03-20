package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.postgres.Device;
import com.redroundrobin.thirema.apirest.models.postgres.Gateway;
import com.redroundrobin.thirema.apirest.models.postgres.Sensor;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.SensorService;
import com.redroundrobin.thirema.apirest.service.postgres.DeviceService;
import com.redroundrobin.thirema.apirest.service.postgres.GatewayService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import org.springframework.http.HttpEntity;

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

  @Value("${telegram.url}")
  private String telegramUrl;

  @RequestMapping(value = "/auth", method = RequestMethod.POST)
  public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
    String email = authenticationRequest.getUsername();
    String password = authenticationRequest.getPassword();

    if( email == null || password == null ) {
      return ResponseEntity.status(400).build();  // Bad Request
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, password)
      );
    } catch (BadCredentialsException bce) {
      return ResponseEntity.status(401).build();  // Unauthenticated
    } catch (DisabledException de) {
      return ResponseEntity.status(403).build();  // Unauthorized
    }

    HashMap<String,Object> response = new HashMap<>();
    final User user = userService.findByEmail(email);

    if(user.getTFA()){
      response.put("tfa", true);
    } else {
      response.put("user", user);
    }

    final UserDetails userDetails = userService
        .loadUserByUsername(authenticationRequest.getUsername());
    final String token = jwtTokenUtil.generateToken(userDetails);

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<?> checkTFA(@RequestBody String rawData, @RequestHeader("Authorization") String authorization) {
    JsonObject data = JsonParser.parseString(rawData).getAsJsonObject();

    if( !data.has("auth_code") || data.get("auth_code").getAsString().equals("") ) {
      return ResponseEntity.status(400).build();
    }

    String authCode = data.get("auth_code").getAsString();
    String token = authorization.substring(7);
    User user = userService.findByEmail( jwtTokenUtil.extractUsername(token) );

    Map<String, Object> map = new HashMap<>();
    map.put("chat_id", user.getTelegramChat());
    map.put("auth_code", authCode);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map);
    ResponseEntity<String> response = new RestTemplate().postForEntity(telegramUrl, entity, String.class);

    if( response.getStatusCode().value() == 200 ) {
      return ResponseEntity.ok().build();
    } else {
      return response;
    }
  }

  //funzione di controllo username Telegram e salvataggio chatID
  @GetMapping(value = {"/login/{username:.+}/{chatId:.+}"})
  public int checkUser(@PathVariable("username") String username, @PathVariable("chatId") String chatId) {
    if (userService.findByTelegramName(username) == null)
      return 0;
    if (userService.findByTelegramNameAndTelegramChat(username, chatId) == null)
      return 1;
    return 2;
  }
}

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
