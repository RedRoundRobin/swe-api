package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.config.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequestTelegram;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
  public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest)
      throws Exception {
    String email = authenticationRequest.getUsername();
    String password = authenticationRequest.getPassword();

    if (email == null || password == null) {
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

    final UserDetails userDetails;

    try {
      userDetails = userService
          .loadUserByEmail(authenticationRequest.getUsername());
    } catch (UsernameNotFoundException unfe) {
      return ResponseEntity.status(401).build();
    } catch (UserDisabledException ude) {
      return ResponseEntity.status(403).build();
    }

    String token;

    if (user.getTfa()) {

      Random rnd = new Random();
      int sixDigitsCode = 100000 + rnd.nextInt(900000);

      Map<String, Object> map = new HashMap<>();
      map.put("auth_code", sixDigitsCode); //codice fittizio
      map.put("chat_id", user.getTelegramChat());
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> telegramResponse =
          restTemplate.postForEntity(telegramUrl, entity, String.class);

      response.put("tfa", true);

      token = jwtTokenUtil.generateTfaToken("tfa", sixDigitsCode, userDetails);
    } else {
      response.put("user", user);

      token = jwtTokenUtil.generateToken("webapp", userDetails);
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<?> tfaAuthenticate(@RequestBody String rawData,
                                           @RequestHeader("Authorization") String authorization) {
    JsonObject data = JsonParser.parseString(rawData).getAsJsonObject();

    if (!data.has("auth_code") || data.get("auth_code").getAsString().equals("")) {
      return ResponseEntity.status(400).build();
    }

    int authCode = data.get("auth_code").getAsInt();
    String tfaToken = authorization.substring(7);

    if (!jwtTokenUtil.isTfa(tfaToken)) {
      return ResponseEntity.status(400).build();
    }

    int tokenAuthCode = jwtTokenUtil.extractAuthCode(tfaToken);

    if (authCode == tokenAuthCode) {
      User user = userService.findByEmail(jwtTokenUtil.extractUsername(tfaToken));

      final UserDetails userDetails;

      try {
        userDetails = userService
            .loadUserByEmail(user.getEmail());
      } catch (UsernameNotFoundException unfe) {
        return ResponseEntity.status(401).build();
      } catch (UserDisabledException ude) {
        return ResponseEntity.status(403).build();
      }

      final String token = jwtTokenUtil.generateToken("webapp", userDetails);

      HashMap<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("user", user);

      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(401).build();
    }
  }

  @PostMapping(value = "/check")
  public ResponseEntity<?> tokenValidity(@RequestHeader("Authorization") String authorization) {
    if (authorization != null) {
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

  //funzione di controllo username Telegram e salvataggio chatID
  @PostMapping(value = {"/auth/telegram"})
  public ResponseEntity<?> checkUser(@RequestBody
                                           AuthenticationRequestTelegram authenticationRequest) {
    String telegramName = authenticationRequest.getTelegramName();
    String chatId = authenticationRequest.getTelegramChat();

    int code = 2;
    String token = "";

    if (userService.findByTelegramName(telegramName) == null) {
      code = 0;
    } else if (userService.findByTelegramNameAndTelegramChat(telegramName, chatId) == null) {
      code = 1;

      User user = userService.findByTelegramName(telegramName);
      user.setTelegramChat(chatId);
      userService.save(user);
    }

    try {
      if (code != 0) {
        final UserDetails userDetails = userService.loadUserByTelegramName(telegramName);

        token = jwtTokenUtil.generateToken("telegram", userDetails);
      }
    } catch (UsernameNotFoundException | UserDisabledException ue) {
      code = 0;
    }

    HashMap<String, Object> response = new HashMap<>();
    response.put("code", code);
    response.put("token", token);

    return ResponseEntity.ok(response);
  }
}
