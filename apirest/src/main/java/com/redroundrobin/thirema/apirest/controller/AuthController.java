package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.config.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class AuthController {

  @Autowired
  private CustomAuthenticationManager authenticationManager;

  @Autowired
  private UserService userService;

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


    final UserDetails userDetails = userService
        .loadUserByUsername(authenticationRequest.getUsername());
    String token;

    if(user.getTFA()){

      Random rnd = new Random();
      int sixDigitsCode = 100000 + rnd.nextInt(900000);

      Map<String, Object> map = new HashMap<>();
      map.put("auth_code", sixDigitsCode); //codice fittizio
      map.put("chat_id", user.getTelegramChat());
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> telegramResponse = restTemplate.postForEntity(telegramUrl, entity, String.class);

      response.put("tfa", true);

      token = jwtTokenUtil.generateTfaToken(userDetails, sixDigitsCode);
    } else {
      response.put("user", user);

      token = jwtTokenUtil.generateToken(userDetails);
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<?> checkTFA(@RequestBody String rawData, @RequestHeader("Authorization") String authorization) {
    JsonObject data = JsonParser.parseString(rawData).getAsJsonObject();

    if( !data.has("auth_code") || data.get("auth_code").getAsString().equals("") ) {
      return ResponseEntity.status(400).build();
    }

    int authCode = data.get("auth_code").getAsInt();
    String tfaToken = authorization.substring(7);

    if( !jwtTokenUtil.isTfa(tfaToken) ) {
      return ResponseEntity.status(400).build();
    }

    int tokenAuthCode = jwtTokenUtil.extractAuthCode(tfaToken);

    if( authCode == tokenAuthCode ) {
      User user = userService.findByEmail(jwtTokenUtil.extractUsername(tfaToken));

      final UserDetails userDetails = userService
          .loadUserByUsername(user.getEmail());
      final String token = jwtTokenUtil.generateToken(userDetails);

      HashMap<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("user", user);

      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(401).build();
    }
  }

  @PostMapping(value = "/check")
  public ResponseEntity<?> checkToken(@RequestHeader("Authorization") String authorization) {
    if( authorization != null ) {
      String token = authorization.substring(7);
      HashMap<String, Object> response = new HashMap<>();

      try {
        Date expirationTime = jwtTokenUtil.extractExpiration(token);

        if (expirationTime.after(new Date())) {
          response.put("valid", true);
        } else {
          response.put("valid", false);
        }
      } catch (ExpiredJwtException eje) {
        response.put("valid", false);
      }
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(400).build();
    }
  }
}
