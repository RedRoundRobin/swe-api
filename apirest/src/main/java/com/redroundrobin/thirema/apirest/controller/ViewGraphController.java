package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.ViewGraphService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViewGraphController {

  private JwtUtil jwtTokenUtil;

  private UserService userService;

  private ViewGraphService viewGraphService;

  @Autowired
  public ViewGraphController(JwtUtil jwtTokenUtil, UserService userService, ViewGraphService
      viewGraphService) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.userService = userService;
    this.viewGraphService = viewGraphService;
  }

  //tutti i viewGraphs
  @GetMapping(value = {"/viewGraphs"})
  public ResponseEntity<List<ViewGraph>> getViewGraphs(
      @RequestHeader("authorization") String authorization) {
    User user = userService.findByEmail(jwtTokenUtil.extractUsername(authorization.substring(7)));
    if (user != null) {
      if (user.getType() == User.Role.ADMIN) {
        return ResponseEntity.ok(viewGraphService.findAll());
      }
    }
    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
  }
}
