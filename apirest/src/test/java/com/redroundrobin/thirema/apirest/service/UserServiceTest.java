package com.redroundrobin.thirema.apirest.service;
/*
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserServiceTest {
  @MockBean
  UserRepository userRepo;

  @MockBean
  UserService userService;

  @Test
  public void editByMod() throws EntityNotFoundException, KeysNotFoundException,
      UserRoleNotFoundException {
    User user = new User();
    user.setEmail("email");
    user.setPassword("password");

    User userEdited = user;
    userEdited.setEmail("liame");

    when(userRepo.save(userEdited)).thenReturn(userEdited);

    JsonObject jsonObject = new JsonObject();
    jsonObject.add("email", new JsonPrimitive("liame"));

    User userAfterEdit;
    try {
      userAfterEdit = userService.editByModerator(user, jsonObject);

      assertEquals(userEdited.getEmail(), userAfterEdit.getEmail());
    } catch (NotAllowedToEditException | TfaNotPermittedException natef) {
      assertTrue(false);
    }
  }
}*/
