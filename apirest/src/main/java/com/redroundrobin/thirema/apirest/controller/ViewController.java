package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.postgres.ViewService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewController {

  private JwtUtil jwtTokenUtil;

  private UserService userService;

  private ViewService viewService;

  @Autowired
  public ViewController(JwtUtil jwtTokenUtil, UserService userService,
                        ViewService viewService) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
    this.viewService = viewService;
  }

  @GetMapping(value = {"/views"})
  public ResponseEntity<?> views(@RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    return ResponseEntity.ok(viewService.findByUserId(user.getUserId()));
  }


  @GetMapping(value = {"/view/{viewId:.+}"})
  public ResponseEntity<?> selectOneView(
      @RequestHeader("Authorization") String authorization,  @PathVariable("viewId") int viewId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    View view = viewService.findByViewId(viewId);
    if(view != null && user.getUserId() == view.getViewId())
      return ResponseEntity.ok(view);
    if(!(view != null))
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }


  /*@PostMapping(name = "/views/create")
  public ResponseEntity<?> views(
      @RequestHeader("Authorization") String authorization,  @RequestBody String rawNewView) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    JsonObject jsonNewView = JsonParser.parseString(rawNewView).getAsJsonObject();
    if(jsonNewView.has("name")){
      View view = new View(); //va bene costruire qua l'oggetto view?
      view.setName(newView);
      return ResponseEntity.ok(view);
    }
    return new ResponseEntity(HttpStatus.FORBIDDEN); //risposta troppo generica...? Metto nel suo body
    //qualcosa di piu descrittivo!!!
  }*/

 /* @DeleteMapping(name = "/views/delete/{viewId:.+}")
  public ResponseEntity<?> deleteView(
      @RequestHeader("Authorization") String authorization,  @PathVariable("name") String newView) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(token));
    View view = viewService.findById(viewId);
    if(view != null)
      return ResponseEntity.ok(view.deleteView(viewId));
    return ResponseEntity.status(HttpStatus.FORBIDDEN); //risposta troppo generica...? Metto nel suo body
    //qualcosa di piu descrittivo!!!
  }*/
}
