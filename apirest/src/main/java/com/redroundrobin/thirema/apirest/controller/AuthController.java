package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.config.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.TelegramService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private CustomAuthenticationManager authenticationManager;

  private UserService userService;

  private JwtUtil jwtTokenUtil;

  private TelegramService telegramService;

  @Autowired
  public AuthController(CustomAuthenticationManager authenticationManager, UserService userService,
                        JwtUtil jwtTokenUtil, TelegramService telegramService) {
    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.jwtTokenUtil = jwtTokenUtil;
    this.telegramService = telegramService;
  }

  @PostMapping(value = "/auth")
  public ResponseEntity<Map<String, Object>> authentication(
      @RequestBody Map<String, Object> authenticationRequest) {
    String email = (String) authenticationRequest.get("username");
    String password = (String) authenticationRequest.get("password");

    if (email == null || password == null) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);  // Bad Request
    }

    UserDetails userDetails;

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, password)
      );
      userDetails = userService
          .loadUserByEmail(email);
    } catch (BadCredentialsException bce) {
      return new ResponseEntity(HttpStatus.UNAUTHORIZED);  // Unauthenticated
    } catch (DisabledException | UserDisabledException de) {
      return new ResponseEntity(HttpStatus.FORBIDDEN);  // Unauthorized
    }

    HashMap<String,Object> response = new HashMap<>();
    final User user = userService.findByEmail(email);

    String token;

    if (user.getTfa()) {
      if (user.getTelegramChat() != null && !user.getTelegramChat().isEmpty()) {
        Random rnd = new Random();
        int sixDigitsCode = 100000 + rnd.nextInt(900000);

        Map<String, Object> request = new HashMap<>();
        request.put("auth_code", sixDigitsCode); //codice fittizio
        request.put("chat_id", user.getTelegramChat());

        if (!telegramService.sendTfa(request)) {
          return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("tfa", true);

        token = jwtTokenUtil.generateTfaToken("tfa", String.valueOf(sixDigitsCode), userDetails);
      } else {
        return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      response.put("user", user);

      token = jwtTokenUtil.generateToken("webapp", userDetails);
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<Map<String, Object>> tfaAuthentication(
      @RequestBody Map<String, Object> request,
      @RequestHeader("Authorization") String authorization) {

    if (!request.containsKey("auth_code") || ((String) request.get("auth_code")).equals("")) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String authCode = (String) request.get("auth_code");
    String tfaToken = authorization.substring(7);

    if (!jwtTokenUtil.isTfa(tfaToken)) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String tokenAuthCode = jwtTokenUtil.extractAuthCode(tfaToken);

    if (tokenAuthCode.equals(authCode)) {
      User user = userService.findByEmail(jwtTokenUtil.extractUsername(tfaToken));

      final UserDetails userDetails;

      try {
        if (user == null) {
          throw new UsernameNotFoundException("User with email furnished is not found");
        }
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
  public ResponseEntity<Map<String, Object>> telegramAuthentication(@RequestBody
                                           Map<String, Object> authenticationRequest) {
    String telegramName = (String) authenticationRequest.get("telegram_name");
    String chatId = (String) authenticationRequest.get("telegram_chat");

    if (telegramName == null || chatId == null)  {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

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
    } catch (UsernameNotFoundException | UserDisabledException | TelegramChatNotFoundException ue) {
      code = 0;
    }

    HashMap<String, Object> response = new HashMap<>();
    response.put("code", code);
    response.put("token", token);

    return ResponseEntity.ok(response);
  }
}
