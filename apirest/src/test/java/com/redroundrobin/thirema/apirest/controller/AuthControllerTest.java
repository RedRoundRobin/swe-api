package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redroundrobin.thirema.apirest.models.AuthenticationRequest;
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

  private User defaultUser() {
    User user = new User();
    user.setName("user");
    user.setSurname("user");
    user.setEmail("email@test.it");
    user.setPassword("password");
    user.setType(2);

    when(userService.findByEmail(user.getEmail())).thenReturn(user);

    org.springframework.security.core.userdetails.User userD = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    when(userService.loadUserByUsername(user.getEmail())).thenReturn(userD);

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
}