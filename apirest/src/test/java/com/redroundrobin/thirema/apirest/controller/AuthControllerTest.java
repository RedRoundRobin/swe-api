package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.config.CustomAuthenticationManager;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.TelegramService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.UserDisabledException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class AuthControllerTest {

  @MockBean
  private CustomAuthenticationManager authenticationManager;

  @MockBean
  private UserService userService;

  @MockBean
  private JwtUtil jwtTokenUtil;

  @MockBean
  private TelegramService telegramService;

  @MockBean
  private LogService logService;

  private AuthController authController;


  private User admin;
  private User adminTfa1;
  private User adminTfa2;
  private User disabledAdmin;

  @Before
  public void setUp() throws Exception {
    authController = new AuthController(authenticationManager, telegramService);
    authController.setJwtUtil(jwtTokenUtil);
    authController.setLogService(logService);
    authController.setUserService(userService);

    admin = new User();
    admin.setName("admin");
    admin.setSurname("admin");
    admin.setEmail("admin");
    admin.setPassword("password");
    admin.setType(User.Role.ADMIN);

    disabledAdmin = new User();
    disabledAdmin.setName("admin2");
    disabledAdmin.setSurname("admin2");
    disabledAdmin.setEmail("admin2");
    disabledAdmin.setPassword("password");
    disabledAdmin.setType(User.Role.ADMIN);
    disabledAdmin.setDeleted(true);

    adminTfa1 = new User();
    adminTfa1.setName("adminTfa1");
    adminTfa1.setSurname("adminTfa1");
    adminTfa1.setEmail("adminTfa1");
    adminTfa1.setPassword("password");
    adminTfa1.setType(User.Role.ADMIN);
    adminTfa1.setTelegramName("telegramName");
    adminTfa1.setTelegramChat("51916");
    adminTfa1.setTfa(true);

    adminTfa2 = new User();
    adminTfa2.setName("adminTfa2");
    adminTfa2.setSurname("adminTfa2");
    adminTfa2.setEmail("adminTfa2");
    adminTfa2.setPassword("password");
    adminTfa2.setType(User.Role.ADMIN);
    adminTfa2.setTelegramName("telegramName");
    adminTfa2.setTfa(true);

    List<User> users = new ArrayList<>();
    users.add(admin);
    users.add(disabledAdmin);
    users.add(adminTfa1);
    users.add(adminTfa2);

    when(authenticationManager.authenticate(any(Authentication.class))).thenAnswer(i -> {
      Authentication auth = i.getArgument(0);
      User user = users.stream()
          .filter(u -> u.getEmail().equals(auth.getPrincipal()))
          .findFirst()
          .orElse(null);

      if(auth.getPrincipal().equals(user.getEmail())) {
        if(auth.getCredentials().equals(user.getPassword())) {
          if( !user.isDeleted() ) {
            return new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
          } else {
            throw new DisabledException("User is deleted or not have an entity");
          }
        } else {
          throw new BadCredentialsException("User not found or password not match");
        }
      } else {
        throw new BadCredentialsException("User not found or password not match");
      }
    });

    doNothing().when(logService).createLog(anyInt(), anyString(), anyString(), anyString());

    when(jwtTokenUtil.generateTokenWithExpiration(anyString(),any(Date.class),
        any(UserDetails.class))).thenReturn("tokenWithTypeDateAndUserDetailsFurnished");
    when(jwtTokenUtil.generateToken(anyString(),
        any(UserDetails.class))).thenReturn("tokenWithTypeAndUserDetailsFurnished");
    when(jwtTokenUtil.generateTfaToken(anyString(), anyString(),
        any(UserDetails.class))).thenReturn("tfaTokenWithTypeAuthCodeAndUserDetailsFurnished");

    when(userService.findByEmail(anyString())).thenAnswer(i -> {
      String email = i.getArgument(0);
      return users.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
    });
    when(userService.loadUserByEmail(anyString())).thenAnswer(i -> {
      String email = i.getArgument(0);
      User user = users.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);

      if( user != null && !user.isDeleted() ) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

        return new org.springframework.security.core.userdetails.User(user.getEmail(),
            user.getPassword(), grantedAuthorities);
      } else if( user == null ) {
        throw new UsernameNotFoundException("");
      } else {
        throw new UserDisabledException("");
      }
    });
    when(userService.loadUserByTelegramName(anyString())).thenAnswer(i -> {
      String telegramName = i.getArgument(0);
      User user = users.stream().filter(u -> telegramName.equals(u.getTelegramName()))
          .findFirst().orElse(null);

      if( user != null && !user.isDeleted() ) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(user.getType())));

        return new org.springframework.security.core.userdetails.User(user.getTelegramName(),
            user.getTelegramChat(), grantedAuthorities);
      } else if( user == null ) {
        throw new UsernameNotFoundException("");
      } else {
        throw new UserDisabledException("");
      }
    });
  }



  // authentication METHOD TESTS

  @Test
  public void authenticationSuccessfull() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",admin.getEmail());
    request.put("password",admin.getPassword());

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      assertNotEquals("", (String) responseMap.get("token"));
      assertNotNull(responseMap.get("user"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }

  @Test
  public void authenticationBadRequestError400() throws Exception {

    Map<String, Object> request = new HashMap<>();

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void authenticationWrongPasswordFoundError401() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",admin.getEmail());
    request.put("password","wrong");

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  public void authenticationDisabledUserError403() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",disabledAdmin.getEmail());
    request.put("password",disabledAdmin.getPassword());

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void authenticationWithTfaSuccessfull() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",adminTfa1.getEmail());
    request.put("password",adminTfa1.getPassword());

    when(telegramService.sendTfa(any(Map.class))).thenReturn(true);

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      System.out.println(responseMap);
      assertNotNull(responseMap.get("token"));
      assertTrue((boolean) responseMap.get("tfa"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }

  @Test
  public void authenticationWithTfaNoTelegramComunicationError500() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",adminTfa1.getEmail());
    request.put("password",adminTfa1.getPassword());

    when(telegramService.sendTfa(any(Map.class))).thenReturn(false);

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void authenticationWithTfaUserWithoutTelegramChatError500() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("username",adminTfa2.getEmail());
    request.put("password",adminTfa2.getPassword());

    ResponseEntity response = authController.authentication(request, "localhost");

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }



  // tfaAuthentication METHOD TESTS

  @Test
  public void tfaAuthenticationSuccessfull() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();
    request.put("authCode", authCode);

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(true);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn(authCode);
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn(adminTfa1.getEmail());

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      assertNotEquals("", (String) responseMap.get("token"));
      assertNotNull(responseMap.get("user"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }

  @Test
  public void tfaAuthenticationBadRequestError400() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(true);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn(authCode);
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn(adminTfa1.getEmail());

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void tfaAuthenticationBadRequestIsNotTfaError400() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();
    request.put("authCode", authCode);

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(false);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn(authCode);
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn(adminTfa1.getEmail());

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void tfaAuthenticationUserNotFoundError401() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();
    request.put("authCode", authCode);

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(true);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn(authCode);
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn("noUser");

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  public void tfaAuthenticationUserDisabledError403() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();
    request.put("authCode", authCode);

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(true);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn(authCode);
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn(disabledAdmin.getEmail());

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  public void tfaAuthenticationWrongAuthCodeError401() throws Exception {

    String authCode = "code";
    Map<String, Object> request = new HashMap<>();
    request.put("authCode", authCode);

    when(jwtTokenUtil.isTfa(anyString())).thenReturn(true);
    when(jwtTokenUtil.extractAuthCode(anyString())).thenReturn("differentAuthCode");
    when(jwtTokenUtil.extractUsername(anyString())).thenReturn(adminTfa1.getEmail());

    ResponseEntity response = authController.tfaAuthentication(request, "tokennnnnn", "localhost");

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }



  // telegramAuthentication METHOD TESTS

  @Test
  public void telegramAuthenticationSuccessfullCode1() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("telegramName", adminTfa2.getTelegramName());
    request.put("telegramChat", 46516);

    when(userService.findByTelegramName(anyString())).thenReturn(adminTfa2);
    when(userService.findByTelegramNameAndTelegramChat(anyString(),anyString())).thenReturn(null);
    when(userService.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    ResponseEntity response = authController.telegramAuthentication(request, "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      assertNotEquals("", (String) responseMap.get("token"));
      assertEquals(1,(Integer) responseMap.get("code"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }

  @Test
  public void telegramAuthenticationSuccessfullCode2() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("telegramName", adminTfa1.getTelegramName());
    request.put("telegramChat", Integer.parseInt(adminTfa1.getTelegramChat()));

    when(userService.findByTelegramName(anyString())).thenReturn(adminTfa1);
    when(userService.findByTelegramNameAndTelegramChat(anyString(),anyString()))
        .thenReturn(adminTfa1);
    when(userService.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    ResponseEntity response = authController.telegramAuthentication(request, "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      assertNotEquals("", (String) responseMap.get("token"));
      assertEquals(2,(Integer) responseMap.get("code"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }

  @Test
  public void telegramAuthenticationBadRequestError400() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("telegramName", adminTfa2.getTelegramName());

    ResponseEntity response = authController.telegramAuthentication(request, "localhost");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void telegramAuthenticationNoTelegramNameFoundCode0() throws Exception {

    Map<String, Object> request = new HashMap<>();
    request.put("telegramName", adminTfa2.getTelegramName());
    request.put("telegramChat", 456162);

    when(userService.findByTelegramName(anyString())).thenReturn(null);

    ResponseEntity response = authController.telegramAuthentication(request, "localhost");

    assertEquals(HttpStatus.OK, response.getStatusCode());

    try {
      Map<String, Object> responseMap = (HashMap<String, Object>) response.getBody();

      assertEquals("", responseMap.get("token"));
      assertEquals(0,(Integer) responseMap.get("code"));
    } catch (ClassCastException cce) {
      assertTrue(false);
    }
  }
}