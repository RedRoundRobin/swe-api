package com.redroundrobin.thirema.apirest.service;
import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import com.redroundrobin.thirema.apirest.service.postgres.ViewService;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewServiceTest {

  @MockBean
  private ViewRepository viewRepo;

  private ViewService viewService;

  private User admin1;
  private User mod1;
  private User user1;
  private View view1;
  private View view2;
  private View view3;
  private View view4;

  @Before
  public void setUp() {

    viewService = new ViewService(viewRepo);

    admin1 = new User(); //utente a cui non ho dato alcuna vista
    admin1.setId(1);
    admin1.setName("admin1");
    admin1.setSurname("admin1");
    admin1.setEmail("admin1");
    admin1.setTelegramName("TNAdmin1");
    admin1.setPassword("password");
    admin1.setType(User.Role.ADMIN);

    mod1 = new User();
    mod1.setId(3);
    mod1.setName("mod1");
    mod1.setSurname("mod1");
    mod1.setEmail("mod1");
    mod1.setTelegramName("TNmod1");
    mod1.setPassword("password");
    mod1.setType(User.Role.MOD);

    user1 = new User();
    user1.setId(5);
    user1.setName("user1");
    user1.setSurname("user1");
    user1.setEmail("user1");
    user1.setTelegramName("TNuser1");
    user1.setPassword("password");
    user1.setType(User.Role.USER);

    view1= new View();
    view1.setUser(user1);
    view1.setViewId(1);
    view1.setName("view1");

    view2= new View();
    view2.setUser(user1);
    view2.setViewId(2);
    view2.setName("view2");

    view3= new View();
    view3.setUser(mod1);
    view3.setViewId(3);
    view3.setName("view3");

    view4= new View();
    view4.setUser(mod1);
    view4.setViewId(4);
    view4.setName("view4");

    List<View> allViews = new ArrayList<>();
    allViews.add(view1);
    allViews.add(view2);
    allViews.add(view3);
    allViews.add(view4);

    when(viewRepo.findByViewId(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      Optional<View> viewFound = allViews.stream()
          .filter(view -> view.getViewId() == id)
          .findFirst();
      return viewFound.orElse(null);
    });

    when(viewRepo.findAllByUser(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      List<View> views = allViews.stream()
          .filter(view -> view.getUser() == user).collect(Collectors.toList());
      return views;
    });
  }

  @Test
  public void findByViewIdTest() {
    View view = viewService.findByViewId(1);
    assertEquals(view1, view);
  }

  @Test
  public void findAllByUserTest() {
    List<View> views = viewService.findAllByUser(user1);
    assertTrue(!views.isEmpty() && views.size() == 2);
  }

  // 5 == userId dello user che ho chiamato user1
  @Test
  public void getViewByUserIdSuccesfullTest() {
    try {
      viewService. getViewByUserId(5, 1);
      assertTrue(true);
    }
    catch(ViewNotFoundException e) {
      assertTrue(false);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void getViewByUserIdViewNotFoundExceptionTest() {
    try {
      viewService. getViewByUserId(5, 8);
      assertTrue(false);
    }
    catch(ViewNotFoundException e) {
      assertTrue(true);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void getViewByUserIdValuesNotAllowedExceptionTest() {
    try {
      viewService. getViewByUserId(5, 3);
      assertTrue(false);
    }
    catch(ViewNotFoundException e) {
      assertTrue(false);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(true);
    }
  }

  @Test
  public void serializeViewSuccesfulTest() {
    JsonObject rawViewToInsert = new JsonObject();
    rawViewToInsert.addProperty("name", "myView");

    try {
      viewService.serializeView(rawViewToInsert, user1);
      assertTrue(true);
    }
    catch(KeysNotFoundException e) {
      assertTrue(false);
    }
    catch(MissingFieldsException e) {
      assertTrue(false);
    }
  }

  @Test
  public void serializeViewKeysNotFoundExceptionTest() {
    JsonObject rawViewToInsert = new JsonObject();
    rawViewToInsert.addProperty("name", "myView");
    rawViewToInsert.addProperty("id", 7);

    try {
      viewService.serializeView(rawViewToInsert, user1);
      assertTrue(false);
    }
    catch(KeysNotFoundException e) {
      assertTrue(true);
    }
    catch(MissingFieldsException e) {
      assertTrue(false);
    }
  }

  @Test
  public void serializeViewMissingFieldsExceptionTest() {

    JsonObject rawViewToInsert = new JsonObject(); // empty json: {}

    try {
      viewService.serializeView(rawViewToInsert, user1);
      assertTrue(false);
    }
    catch(KeysNotFoundException e) {
      assertTrue(false);
    }
    catch(MissingFieldsException e) {
      assertTrue(true);
    }
  }

  @Test
  public void deleteViewSuccesfulTest() {
    try {
      viewService.deleteView(user1, 1);
      assertTrue(true);
    }
    catch(NotAuthorizedToDeleteUserException e) {
      assertTrue(false);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void deleteViewNotAuthorizedToDeleteUserExceptionTest() {
    try {
      viewService.deleteView(user1, 3);
      assertTrue(false);
    }
    catch(NotAuthorizedToDeleteUserException e) {
      assertTrue(true);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void deleteViewValuesNotAllowedExceptionTest() {
    try {
      viewService.deleteView(user1, 8);
      assertTrue(false);
    }
    catch(NotAuthorizedToDeleteUserException e) {
      assertTrue(false);
    }
    catch(ValuesNotAllowedException e) {
      assertTrue(true);
    }
  }

}

