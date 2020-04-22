package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.TelegramService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.TelegramChatNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final CustomAuthenticationManager authenticationManager;

  private final TelegramService telegramService;

  private final Random rnd;

  @Autowired
  public AuthController(CustomAuthenticationManager authenticationManager,
                        TelegramService telegramService, JwtUtil jwtUtil, LogService logService,
                        UserService userService) {
    super(jwtUtil, logService, userService);
    this.authenticationManager = authenticationManager;
    this.telegramService = telegramService;

    rnd = new SecureRandom();
  }

  @PostMapping(value = "/auth")
  public ResponseEntity<Map<String, Object>> authentication(
      @RequestBody Map<String, Object> request,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);

    String email = (String) request.get("username");
    String password = (String) request.get("password");

    if (email == null || password == null) {
      logger.debug("email and/or password are null");
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
      logger.debug(bce.toString());
      return new ResponseEntity(HttpStatus.UNAUTHORIZED);  // Unauthenticated
    } catch (DisabledException | UserDisabledException de) {
      logger.debug(de.toString());
      return new ResponseEntity(HttpStatus.FORBIDDEN);  // Unauthorized
    }

    HashMap<String,Object> response = new HashMap<>();
    final User user = userService.findByEmail(email);

    String token;

    if (user.getTfa()) {
      if (user.getTelegramChat() != null && !user.getTelegramChat().isEmpty()) {
        int sixDigitsCode = 100000 + rnd.nextInt(900000);

        Map<String, Object> telegramRequest = new HashMap<>();
        telegramRequest.put("authCode", sixDigitsCode); //codice fittizio
        telegramRequest.put("chatId", user.getTelegramChat());

        if (!telegramService.sendTfa(telegramRequest)) {
          logger.debug("ERROR: INTERNAL_SERVER_ERROR. There is a problem with the connection to "
              + "telegram");
          return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("tfa", true);

        token = jwtUtil.generateTfaToken("tfa", userDetails, String.valueOf(sixDigitsCode));

        logService.createLog(user.getId(), ip, "auth.tfa", "sent tfa code");
      } else {
        logger.debug("ERROR: INTERNAL_SERVER_ERROR. User " + user.getId() + " telegram chat is null"
            + " or empty");
        return  new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    } else {
      response.put("user", user);

      token = jwtUtil.generateToken("webapp", userDetails);

      logService.createLog(user.getId(), ip, "auth.login", "webapp");
    }

    response.put("token", token);

    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/auth/tfa")
  public ResponseEntity<Map<String, Object>> tfaAuthentication(
      @RequestBody Map<String, Object> request,
      @RequestHeader("Authorization") String authorization,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);
    if (!request.containsKey("authCode") || ((String) request.get("authCode")).equals("")) {
      logger.debug("ERROR: BAD_REQUEST. The request not contain authCode or is empty");
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String authCode = (String) request.get("authCode");
    String tfaToken = authorization.substring(7);

    if (!jwtUtil.isTfa(tfaToken)) {
      logger.debug("ERROR: BAD_REQUEST. The token furnished is not a tfa token");
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
        logger.debug(unfe.toString());
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
      } catch (UserDisabledException ude) {
        logger.debug(ude.toString());
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }

      final String token = jwtUtil.generateToken("webapp", userDetails);

      HashMap<String, Object> response = new HashMap<>();
      response.put("token", token);
      response.put("user", user);

      logService.createLog(user.getId(), ip, "auth.login", "tfa confirmed");

      return ResponseEntity.ok(response);
    } else {
      logger.debug("ERROR: UNAUTHORIZED. The authCode furnished from the client is different from "
          + "the one coded in the token");
      return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }
  }

  //funzione di controllo username Telegram e salvataggio chatID
  @PostMapping(value = {"/auth/telegram"})
  public ResponseEntity<Map<String, Object>> telegramAuthentication(
      @RequestBody Map<String, Object> request,
      HttpServletRequest httpRequest) {
    String ip = getIpAddress(httpRequest);

    String telegramName = (String) request.get("telegramName");
    Integer intChatId = (Integer) request.get("telegramChat");

    User user = userService.findByTelegramName(telegramName);

    if (telegramName == null || intChatId == null)  {
      logger.debug("ERROR: BAD_REQUEST. TelegramName and/or intChatId are null");
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    String chatId = intChatId.toString();

    int code = 2;
    String token = "";

    if (userService.findByTelegramName(telegramName) == null) {
      code = 0;
    } else if (userService.findByTelegramNameAndTelegramChat(telegramName, chatId) == null) {
      code = 1;

      userService.editUserTelegramChat(user, chatId);
    }

    try {
      if (code != 0) {
        final UserDetails userDetails = userService.loadUserByTelegramName(telegramName);

        token = jwtUtil.generateToken("telegram", userDetails);

        logService.createLog(user.getId(), ip, "auth.login", "telegram");
      }
    } catch (UsernameNotFoundException | UserDisabledException | TelegramChatNotFoundException ue) {
      logger.debug(ue.toString());
      code = 0;
    }

    HashMap<String, Object> response = new HashMap<>();
    response.put("code", code);
    response.put("token", token);

    return ResponseEntity.ok(response);
  }
}
