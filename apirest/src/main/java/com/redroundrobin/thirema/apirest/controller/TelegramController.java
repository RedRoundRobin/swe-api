package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelegramController {

  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtil jwtTokenUtil;

  public TelegramController(JwtUtil jwtTokenUtil, UserService userService) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
  }

  //funzione di controllo username Telegram e salvataggio chatID
  @GetMapping(value = {"/status"})
  public ResponseEntity<User> checkStatus(@RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);

    User user = userService.findByTelegramName(jwtTokenUtil.extractUsername(token));
    user.setTelegramChat("");
    user.setTelegramName("");

    return ResponseEntity.ok(user);
  }
}

/*
 * le risorse sono identificabili tramite url
 * le operazioni devono essere implementati tramite metodi appropiati
 * la rappresentazione delle risorse devono essere tramite un formato standard, specificato nel body
 */
