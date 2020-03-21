package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  UserService userService;

  @TestConfiguration
  static class AdditionalConfig {
    @Bean
    public JwtUtil getSomeBean() {
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

    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    org.springframework.security.core.userdetails.User userD = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());

    when(userService.loadUserByEmail(user.getEmail())).thenReturn(userD);

    return user;
  }

  @Test
  public void normalAuth() throws Exception {
    User user = defaultUser();

    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mockMvc.perform(
      MockMvcRequestBuilders
          .post("/auth")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotEquals(response.get("token").getAsString(),"");
    assertNotNull(response.get("user"));
  }

  @Test
  public void addUserToDB_error401() throws Exception {

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
  public void addUserToDB_error403() throws Exception {

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
  /*@Test
  public void addUserToDB_receive2FA() throws Exception {

    User user = this.defaultUser();
    user.setTFA(true);
    user.setTelegramName("prova");

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
  }*/
}