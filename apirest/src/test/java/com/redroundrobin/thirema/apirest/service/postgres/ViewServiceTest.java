package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewServiceTest {

  private ViewService viewService;

  @MockBean
  private ViewRepository viewRepo;

  @MockBean
  private UserRepository userRepo;

  private User admin1;
  private User mod1;
  private User user1;
  private View view1;
  private View view2;
  private View view3;
  private View view4;

  @Before
  public void setUp() {

    viewService = new ViewService(viewRepo, userRepo);

    // ----------------------------------------- Set Users ---------------------------------------
    admin1 = new User(1, "admin1", "admin1", "admin1", "pass", User.Role.ADMIN);
    mod1 = new User(3, "mod1", "mod1", "mod1", "pass", User.Role.MOD);
    user1 = new User(5, "user1", "user1", "user1", "pass", User.Role.USER);

    List<User> allUsers = new ArrayList<>();
    allUsers.add(user1);
    allUsers.add(mod1);
    allUsers.add(admin1);

    // ----------------------------------------- Set Views ---------------------------------------
    view1 = new View(1,"view1", user1);
    view2 = new View(2,"view2", user1);
    view3 = new View(3,"view3", mod1);
    view4 = new View(4,"view4", mod1);

    List<View> allViews = new ArrayList<>();
    allViews.add(view1);
    allViews.add(view2);
    allViews.add(view3);
    allViews.add(view4);

    when(viewRepo.findById(anyInt())).thenAnswer(i -> {
      int id = i.getArgument(0);
      return allViews.stream()
          .filter(view -> view.getId() == id)
          .findFirst();
    });
    when(viewRepo.findAllByUser(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return allViews.stream()
          .filter(view -> view.getUser() == user).collect(Collectors.toList());
    });
    when(viewRepo.findByViewIdAndUser(anyInt(), any(User.class))).thenAnswer(i -> allViews.stream()
        .filter(v -> i.getArgument(0).equals(v.getId())
            && i.getArgument(1).equals(v.getUser())).findFirst().orElse(null));

    when(userRepo.findById(anyInt())).thenAnswer(i -> allUsers.stream()
      .filter(u -> i.getArgument(0).equals(u.getId())).findFirst());
  }

  @Test
  public void findByViewIdTest() {
    View view = viewService.findById(1);
    assertEquals(view1, view);
  }

  @Test
  public void findAllByUserTest() {
    List<View> views = viewService.findAllByUser(user1);
    assertEquals(2, views.size());
  }

  @Test
  public void findViewByIdAndUserId() {
    View view = viewService.findByIdAndUserId(view1.getId(), user1.getId());

    assertNotNull(view);
  }

  @Test
  public void findViewByIdAndNotExistentUserId() {
    View view = viewService.findByIdAndUserId(view1.getId(), 9);

    assertNull(view);
  }

  @Test
  public void serializeViewSuccesfulTest() {
    JsonObject rawViewToInsert = new JsonObject();
    rawViewToInsert.addProperty("name", "myView");

    try {
      viewService.addView(rawViewToInsert, user1);
      assertTrue(true);
    }
    catch(KeysNotFoundException | MissingFieldsException e) {
      fail();
    }
  }

  @Test
  public void serializeViewKeysNotFoundExceptionTest() {
    JsonObject rawViewToInsert = new JsonObject();
    rawViewToInsert.addProperty("name", "myView");
    rawViewToInsert.addProperty("id", 7);

    try {
      viewService.addView(rawViewToInsert, user1);
      fail();
    }
    catch(KeysNotFoundException e) {
      assertTrue(true);
    }
    catch(MissingFieldsException e) {
      fail();
    }
  }

  @Test
  public void serializeViewMissingFieldsExceptionTest() {

    JsonObject rawViewToInsert = new JsonObject(); // empty json: {}

    try {
      viewService.addView(rawViewToInsert, user1);
      fail();
    }
    catch(KeysNotFoundException e) {
      fail();
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
    catch(NotAuthorizedException | InvalidFieldsValuesException e) {
      fail();
    }
  }

  @Test
  public void deleteViewNotAuthorizedToDeleteUserExceptionTest() {
    try {
      viewService.deleteView(user1, 3);
      fail();
    }
    catch(NotAuthorizedException e) {
      assertTrue(true);
    }
    catch(InvalidFieldsValuesException e) {
      fail();
    }
  }

  @Test
  public void deleteViewValuesNotAllowedExceptionTest() {
    try {
      viewService.deleteView(user1, 8);
      fail();
    }
    catch(NotAuthorizedException e) {
      fail();
    }
    catch(InvalidFieldsValuesException e) {
      assertTrue(true);
    }
  }

}
