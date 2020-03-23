package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import java.util.ArrayList;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  UserService userService;

  @Autowired
  JwtUtil jwtTokenUtil;

  @TestConfiguration
  static class AdditionalConfig {
    @Bean
    public JwtUtil getJwtTokenUtilBean() {
      return new JwtUtil();
    }
  }

  private User defaultUser() throws UsernameNotFoundException, UserDisabledException {
    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(2);

    org.springframework.security.core.userdetails.User userD = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());

    when(userService.findByEmail(user.getEmail())).thenReturn(user);
    when(userService.loadUserByEmail(user.getEmail())).thenReturn(userD);

    return user;
  }

  @Test
  public void authenticateSuccessfull() throws Exception {
    User user = defaultUser();

    JSONObject json = new JSONObject();
    json.put("username",user.getEmail());
    json.put("password",user.getPassword());
    System.out.println(json.toString());

    MvcResult mvcResult = mockMvc.perform(
      MockMvcRequestBuilders
          .post("/auth")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(json.toString()))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotEquals(response.get("token").getAsString(),"");
    assertNotNull(response.get("user"));
  }

  @Test
  public void authenticateError400() throws Exception {

    this.defaultUser();

    String uri = "/auth";
    JsonObject request = new JsonObject();

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(request.toString()))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(400, status);
  }

  @Test
  public void authenticateError401() throws Exception {

    User user = this.defaultUser();

    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword()+"1");

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(401, status);
  }

  @Test
  public void authenticateError403() throws Exception {

    User user = this.defaultUser();
    user.setDeleted(true);

    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(403, status);
  }

  // Test that work only with telegram
  @Test
  public void authenticateReceiveTfaToken() throws Exception {

    User user = this.defaultUser();
    user.setTfa(true);
    user.setTelegramName("prova");
    user.setTelegramChat("4365587567");

    // Creating request to api
    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(inputJson))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = gson.fromJson(
        mvcResult.getResponse().getContentAsString(), JsonObject.class);

    assertTrue(response.has("tfa"));
    assertTrue(response.has("token"));

    String token = response.get("token").getAsString();
  }

  // Test that work only with telegram
  @WithMockUser
  @Test
  public void authenticateTfa() throws Exception {

    User user = this.defaultUser();

    String authCode = "456436";

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateTfaToken("tfa", authCode, userDetails);

    // Creating request to api
    String uri = "/auth/tfa";

    JSONObject request = new JSONObject();
    request.put("auth_code", authCode);

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .post(uri)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotEquals(response.get("token").getAsString(),"");
    assertNotNull(response.get("user"));
  }
}