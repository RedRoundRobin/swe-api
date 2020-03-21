package com.redroundrobin.thirema.apirest.controller;
/*
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.ApirestApplication;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= ApirestApplication.class)
@EnableTransactionManagement
@AutoConfigureMockMvc
public class PostgreControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  UserRepository userRepository;

  @Test
  @Transactional("postgresTransactionManager")
  public void addUserToDBTest() {
    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(0);

    userRepository.save(user);

    assertNotNull(userRepository.findByEmail("email@test.it"));
  }

  @Test
  @Transactional("postgresTransactionManager")
  public void addUserToDB_authenticateAndReceiveToken() throws Exception {

    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(2);

    userRepository.save(user);

    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mvc.perform(
        MockMvcRequestBuilders.post(uri)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = gson.fromJson(  mvcResult.getResponse().getContentAsString(), JsonObject.class );

    assertNotEquals(response.get("token"),"");
    assertNotNull(response.get("user"));
  }

  @Test
  @Transactional("postgresTransactionManager")
  public void addUserToDB_error401() throws Exception {

    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(2);

    user = userRepository.save(user);

    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword()+"1");

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(401, status);
  }

  @Test
  @Transactional("postgresTransactionManager")
  public void addUserToDB_error403() throws Exception {

    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(0);

    userRepository.save(user);

    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mvc.perform(
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(inputJson))
        .andReturn();

    int status = mvcResult.getResponse().getStatus();
    assertEquals(403, status);
  }

  @Test
  @Transactional("postgresTransactionManager")
  public void addUserToDB_receive2FA() throws Exception {
    // User db insert
    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(2);
    user.setTFA(true);
    user.setTelegramName("prova");

    user = userRepository.save(user);


    // Creating request to api
    String uri = "/auth";
    AuthenticationRequest authenticationRequest = new AuthenticationRequest(user.getEmail(),user.getPassword());

    Gson gson = new Gson();
    String inputJson = gson.toJson(authenticationRequest);

    MvcResult mvcResult = mvc.perform(
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
}*/