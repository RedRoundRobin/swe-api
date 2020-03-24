package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.redroundrobin.thirema.apirest.models.UserDisabledException;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.service.postgres.EntityService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.NotAllowedToEditFields;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  UserService userService;

  @MockBean
  EntityService entityService;

  @Autowired
  JwtUtil jwtTokenUtil;

  @TestConfiguration
  static class AdditionalConfig {
    @Bean
    public JwtUtil getJwtTokenUtilBean() {
      return new JwtUtil();
    }
  }

  private User adminUser() throws UsernameNotFoundException, UserDisabledException, NotAllowedToEditFields {
    User editingUser = new User();
    editingUser.setName("user");
    editingUser.setSurname("user");
    editingUser.setEmail("email@test.it");
    editingUser.setPassword("password");
    editingUser.setType(User.Role.ADMIN);

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(editingUser.getType())));

    org.springframework.security.core.userdetails.User userD =
        new org.springframework.security.core.userdetails.User(editingUser.getEmail(),
            editingUser.getPassword(), grantedAuthorities );

    when(userService.findByEmail(editingUser.getEmail())).thenReturn(editingUser);
    when(userService.loadUserByEmail(editingUser.getEmail())).thenReturn(userD);

    when(userService.find(1)).thenReturn(editingUser);
    when(userService.editByAdministrator(eq(editingUser), any(JsonObject.class))).thenReturn(editingUser);

    return editingUser;
  }

  private User modUser() throws UsernameNotFoundException, UserDisabledException, NotAllowedToEditFields {
    Entity entity = new Entity();
    entity.setEntityId(1);

    User editingUser = new User();
    editingUser.setUserId(1);
    editingUser.setName("user");
    editingUser.setSurname("user");
    editingUser.setEmail("email@test.it");
    editingUser.setPassword("password");
    editingUser.setType(User.Role.MOD);
    editingUser.setEntity(entity);

    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
    grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(editingUser.getType())));

    org.springframework.security.core.userdetails.User userD =
        new org.springframework.security.core.userdetails.User(editingUser.getEmail(),
            editingUser.getPassword(), grantedAuthorities );

    when(userService.findByEmail(editingUser.getEmail())).thenReturn(editingUser);
    when(userService.loadUserByEmail(editingUser.getEmail())).thenReturn(userD);

    return editingUser;
  }

  @WithMockUser
  @Test
  public void editAdminByAdminSuccessfull() throws Exception {

    User user = this.adminUser();

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    when(userService.editByAdministrator(eq(user), any(JsonObject.class))).thenReturn(user);

    // Creating request to api
    String uri = "/users/1/edit";

    JSONObject request = new JSONObject();
    request.put("telegram_name", "ciao");

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotNull(response.get("userId"));
  }

  @WithMockUser
  @Test
  public void editAdminByAdminError400() throws Exception {

    User user = this.adminUser();

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    // Creating request to api
    String uri = "/users/1/edit";

    JSONObject request = new JSONObject();
    request.put("user_id", 1);

    JsonObject json = new JsonObject();
    json.add("user_id", new JsonPrimitive(1));
    when(userService.editByAdministrator(user, json)).thenThrow(new NotAllowedToEditFields(""));

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(400, status);
  }

  @WithMockUser
  @Test
  public void editModItselfSuccessfull() throws Exception {

    User user = this.modUser();

    when(userService.find(1)).thenReturn(user);
    when(userService.editItself(eq(user), any(JsonObject.class))).thenReturn(user);

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    // Creating request to api
    String uri = "/users/1/edit";

    JsonObject request = new JsonObject();
    request.add("telegram_name", new JsonPrimitive("ciao"));

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotNull(response.get("userId"));
  }

  @WithMockUser
  @Test
  public void editUserByModSuccessfull() throws Exception {

    User user = this.modUser();

    User user2 = new User();
    user2.setUserId(2);
    user2.setName("user");
    user2.setSurname("user");
    user2.setEmail("email1@test.it");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(user.getEntity());

    when(userService.find(2)).thenReturn(user2);
    when(userService.editByModerator(eq(user2), any(JsonObject.class))).thenReturn(user2);

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    // Creating request to api
    String uri = "/users/2/edit";

    JsonObject request = new JsonObject();
    request.add("telegram_name", new JsonPrimitive("ciao"));

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(200, status);

    JsonObject response = JsonParser.parseString( mvcResult.getResponse().getContentAsString() ).getAsJsonObject();
    assertNotNull(response.get("userId"));
  }

  @WithMockUser
  @Test
  public void editUserByModError403() throws Exception {

    User user = this.modUser();

    Entity entity1 = new Entity();
    entity1.setEntityId(2);

    User user2 = new User();
    user2.setUserId(3);
    user2.setName("user");
    user2.setSurname("user");
    user2.setEmail("email2@test.it");
    user2.setPassword("password");
    user2.setType(User.Role.USER);
    user2.setEntity(entity1);

    when(userService.find(3)).thenReturn(user2);
    when(userService.editByModerator(eq(user2), any(JsonObject.class))).thenThrow(new NotAllowedToEditFields(""));

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    // Creating request to api
    String uri = "/users/3/edit";

    JsonObject request = new JsonObject();
    request.add("telegram_name", new JsonPrimitive("ciao"));

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(403, status);
  }

  @WithMockUser
  @Test
  public void editUserNotFoundError400() throws Exception {

    User user = this.modUser();

    UserDetails userDetails = userService.loadUserByEmail(user.getEmail());

    String tfaToken = jwtTokenUtil.generateToken("webapp", userDetails);

    // Creating request to api
    String uri = "/users/3/edit";

    JsonObject request = new JsonObject();
    request.add("telegram_name", new JsonPrimitive("ciao"));

    MvcResult mvcResult = mockMvc.perform(
        MockMvcRequestBuilders
            .put(uri)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization","Bearer "+tfaToken)
            .content(request.toString()))
        .andReturn();

    // Check status and if are present tfa and token
    int status = mvcResult.getResponse().getStatus();
    assertEquals(400, status);
  }
}