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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
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
  public ResponseEntity<?> authentication(@RequestBody AuthenticationRequest authenticationRequest)
      throws Exception {
    String email = authenticationRequest.getUsername();
    String password = authenticationRequest.getPassword();

    if (email == null || password == null) {
      return ResponseEntity.status(400).build();  // Bad Request
    }

    UserDetails userDetails;

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, password)
      );
      userDetails = userService
          .loadUserByEmail(authenticationRequest.getUsername());
    } catch (BadCredentialsException bce) {
      return ResponseEntity.status(401).build();  // Unauthenticated
    } catch (DisabledException de) {
      return ResponseEntity.status(403).build();  // Unauthorized
    }

    HashMap<String,Object> response = new HashMap<>();
    final User user = userService.findByEmail(email);

    String token;

    if (user.getTfa()) {
      if (user.getTelegramChat() != null && !user.getTelegramChat().isEmpty()) {
        Random rnd = new Random();
        int sixDigitsCode = 100000 + rnd.nextInt(900000);

        Map<String, Object> map = new HashMap<>();
        map.put("auth_code", sixDigitsCode); //codice fittizio
        map.put("chat_id", user.getTelegramChat());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map);

        try {
          RestTemplate restTemplate = new RestTemplate();
          ResponseEntity<String> telegramResponse =
              restTemplate.postForEntity(telegramUrl, entity, String.class);

          if (telegramResponse.getStatusCode().value() != 200) {
            throw new ResourceAccessException("");
          }
        } catch (RestClientResponseException | ResourceAccessException rae) {
          return ResponseEntity.status(500).build();
        }

        response.put("tfa", true);

        token = jwtTokenUtil.generateTfaToken("tfa", String.valueOf(sixDigitsCode), userDetails);
      } else {
        return ResponseEntity.status(500).build();
      }
    } else {
      response.put("user", user);

      token = jwtTokenUtil.generateToken("webapp", userDetails);
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<?> tfaAuthentication(@RequestBody String rawData,
                                           @RequestHeader("Authorization") String authorization) {
    JsonObject data = JsonParser.parseString(rawData).getAsJsonObject();

    if (!data.has("auth_code") || data.get("auth_code").getAsString().equals("")) {
      return ResponseEntity.status(400).build();
    }

    String authCode = data.get("auth_code").getAsString();
    String tfaToken = authorization.substring(7);

    if (!jwtTokenUtil.isTfa(tfaToken)) {
      return ResponseEntity.status(400).build();
    }

    String tokenAuthCode = jwtTokenUtil.extractAuthCode(tfaToken);

    if (tokenAuthCode.equals(authCode)) {
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

  //funzione di controllo username Telegram e salvataggio chatID
  @PostMapping(value = {"/auth/telegram"})
  public ResponseEntity<?> telegramAuthentication(@RequestBody
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
