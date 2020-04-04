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
public class AuthController extends CoreController {

  private CustomAuthenticationManager authenticationManager;

  private TelegramService telegramService;

  @Autowired
  public AuthController(CustomAuthenticationManager authenticationManager,
                        TelegramService telegramService) {
    this.authenticationManager = authenticationManager;
    this.telegramService = telegramService;
  }

  @PostMapping(value = "/auth")
  public ResponseEntity<Map<String, Object>> authentication(
      @RequestBody Map<String, Object> authenticationRequest,
      @RequestHeader(value = "X-Forwarded-For") String ip) {
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
        request.put("authCode", sixDigitsCode); //codice fittizio
        request.put("chatId", user.getTelegramChat());

        if (!telegramService.sendTfa(request)) {
          return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("tfa", true);

        token = jwtUtil.generateTfaToken("tfa", String.valueOf(sixDigitsCode), userDetails);

        logService.createLog(user.getId(), ip, "login", "sent tfa code");
      } else {
        return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      response.put("user", user);

      token = jwtUtil.generateToken("webapp", userDetails);

      logService.createLog(user.getId(), ip, "login", "webapp");
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<Map<String, Object>> tfaAuthentication(
      @RequestBody Map<String, Object> request,
      @RequestHeader("Authorization") String authorization,
      @RequestHeader(value = "X-Forwarded-For") String ip) {

    if (!request.containsKey("authCode") || ((String) request.get("authCode")).equals("")) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String authCode = (String) request.get("authCode");
    String tfaToken = authorization.substring(7);

    if (!jwtUtil.isTfa(tfaToken)) {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String tokenAuthCode = jwtUtil.extractAuthCode(tfaToken);

    if (tokenAuthCode.equals(authCode)) {
      User user = userService.findByEmail(jwtUtil.extractUsername(tfaToken));

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

      final String token = jwtUtil.generateToken("webapp", userDetails);

      HashMap<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("user", user);

      logService.createLog(user.getId(), ip, "login", "tfa confirmed");

      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(401).build();
    }
  }

  //funzione di controllo username Telegram e salvataggio chatID
  @PostMapping(value = {"/auth/telegram"})
  public ResponseEntity<Map<String, Object>> telegramAuthentication(
      @RequestBody Map<String, Object> authenticationRequest,
      @RequestHeader(value = "X-Forwarded-For") String ip) {
    String telegramName = (String) authenticationRequest.get("telegramName");
    Integer intChatId = (Integer) authenticationRequest.get("telegramChat");

    User user = userService.findByTelegramName(telegramName);

    if (telegramName == null || intChatId == null)  {
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String chatId = intChatId.toString();

    int code = 2;
    String token = "";

    if (userService.findByTelegramName(telegramName) == null) {
      code = 0;
    } else if (userService.findByTelegramNameAndTelegramChat(telegramName, chatId) == null) {
      code = 1;

      user.setTelegramChat(chatId);
      userService.save(user);
    }

    try {
      if (code != 0) {
        final UserDetails userDetails = userService.loadUserByTelegramName(telegramName);

        token = jwtUtil.generateToken("telegram", userDetails);

        logService.createLog(user.getId(), ip, "login", "telegram");
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
