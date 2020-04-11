package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.postgres.ViewService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ViewControllerTest {

  @MockBean
  JwtUtil jwtTokenUtil;

  @MockBean
  private LogService logService;

  @MockBean
  private UserService userService;

  @MockBean
  ViewService viewService;

  private ViewController viewController;
  private User user;
  private User mod;
  private View view1;
  private View view2;
  private View view3;
  private final String userToken = "userToken";
  private final String modToken = "modToken";
  private List<View> allViews;

  @Before
  public void setUp() throws Exception {

    viewController = new ViewController(viewService, jwtTokenUtil, logService, userService);

    // ---------------------------------------- Set Entities --------------------------------------
    new Entity(1, "entity1", "loc1");

    // ---------------------------------------- Set Entities --------------------------------------
    mod = new User(1, "mod", "admin", "admin", "pass", User.Role.MOD);
    user = new User(2, "user", "user", "user", "user", User.Role.USER);

    List<User> allUsers = new ArrayList<>();
    allUsers.add(user);
    allUsers.add(mod);

    // ---------------------------------------- Set Entities --------------------------------------
    view1 = new View(1, "view1", user);
    view2 = new View(2, "view2", user);
    view3 = new View(3, "view3", mod);

    allViews = new ArrayList<>();
    allViews.add(view1);
    allViews.add(view2);
    allViews.add(view3);

    when(jwtTokenUtil.extractRole("Bearer "+userToken)).thenReturn(user.getType());
    when(jwtTokenUtil.extractUsername(userToken)).thenReturn(user.getEmail());
    when(userService.findByEmail(user.getEmail())).thenReturn(user);
    when(userService.findById(user.getId())).thenReturn(user);

    when(viewService.findAllByUser(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return allViews.stream()
        .filter(view -> view.getUser() == user)
        .collect(Collectors.toList());
  });
    when(viewService.findById(anyInt())).thenAnswer(i -> {
      int viewId = i.getArgument(0);
      Optional<View> retView = allViews.stream()
          .filter(view -> view.getId() == viewId)
          .findFirst();
      return retView.orElse(null);
    });
    when(viewService.findByIdAndUserId(anyInt(), anyInt())).thenAnswer(i -> {
      User user = allUsers.stream()
          .filter(u -> i.getArgument(1).equals(u.getId())).findFirst().orElse(null);
      if (user != null) {
        return allViews.stream().filter(v -> i.getArgument(0).equals(v.getId())
            && v.getUser().equals(user)).findFirst().orElse(null);
      } else {
        return null;
      }
    });
    when(viewService.serializeView(any(JsonObject.class), any(User.class))).thenAnswer(i -> {
      JsonObject jsonViewToCreate = i.getArgument(0);
      User user = i.getArgument(1);
      if(jsonViewToCreate.has("name") &&
          jsonViewToCreate.keySet().size() == 1) {
        View newView = new View();
        newView.setName(jsonViewToCreate.get("name").getAsString());
        newView.setUser(user);
        allViews.add(newView);
        return newView;
      }
      if(jsonViewToCreate.keySet().stream().allMatch(key -> key.equals("name")))
        throw new MissingFieldsException("Some necessary" +
            " fields are missing: cannot create user");
      else throw new KeysNotFoundException("");
    });
    doAnswer(i -> {
      User deletingUser =  i.getArgument(0);
      int viewToDeleteId = i.getArgument(1);
      View viewToDelete = allViews.stream()
          .filter(view -> view.getId() == viewToDeleteId)
          .findFirst().orElse(null);

      if(viewToDelete == null) {
        throw new InvalidFieldsValuesException("The given view_id" +
            " doesn't correspond to any view");
      }

      else if(viewToDelete.getUser().getId() != deletingUser.getId()) {
        throw new NotAuthorizedException("This user cannot delete the view with" +
            "the view_id given");
      }
      else {
        allViews.remove(viewToDelete);
        return null;
      }
    }).when(viewService).deleteView(any(User.class), anyInt());
  }

  //@GetMapping(value = {"/views"}) test
  @Test
  public void getViewsTest() {
    String authorization = "Bearer "+userToken;
    ResponseEntity<List<View>> rsp = viewController.views(authorization);
    assertSame(HttpStatus.OK, rsp.getStatusCode());
    assertFalse(rsp.getBody().isEmpty());
  }

  //@GetMapping(value = {"/views/{viewId:.+}"}) test
  @Test
  public void selectOneViewSuccesfulTest() {
    String authorization = "Bearer "+userToken;

    when(viewService.findByIdAndUserId(eq(1), eq(3))).thenAnswer(i -> {
        int viewId = i.getArgument(1);
        return viewService.findById(viewId);
    });
      //attenzione a passare in questo test una view che appartine allo user sotto test!
    ResponseEntity<View> rsp = viewController.selectOneView(authorization, 1);
    assertSame(HttpStatus.OK, rsp.getStatusCode());
    assertNotNull(rsp.getBody());
  }

  //@GetMapping(value = {"/views/{viewId:.+}"}) test
  @Test
  public void selectOneViewWithNotExistent() {
    String authorization = "Bearer "+userToken;

    when(viewService.findByIdAndUserId(eq(4), eq(3))).thenReturn(null);

    ResponseEntity<View> rsp  = viewController.selectOneView(authorization, 4);
    assertSame(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }

  //@PostMapping(value = "/views/create")
  @Test
  public void createViewSuccesfulTest() {
    String authorization = "Bearer "+userToken;
    JsonObject jsonViewToCreate = new JsonObject();
    jsonViewToCreate.addProperty("name", "myView");
    String rawViewToCreate = jsonViewToCreate.toString();
    ResponseEntity<View> rsp  = viewController.createView(authorization, rawViewToCreate);
    assertSame(HttpStatus.OK, rsp.getStatusCode());
    assertNotNull(rsp.getBody());
  }

  //@PostMapping(value = "/views/create")
  @Test
  public void createViewMissingFieldsExceptionTest() {
    String authorization = "Bearer "+userToken;
    JsonObject jsonViewToCreate = new JsonObject(); // empty json
    String rawViewToCreate = jsonViewToCreate.toString();
    ResponseEntity<View> rsp  = viewController.createView(authorization, rawViewToCreate);
    assertSame(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }

  //@PostMapping(value = "/views/create")
  @Test
  public void createViewKeysNotFoundExceptionTest() {
    String authorization = "Bearer "+userToken;
    JsonObject jsonViewToCreate = new JsonObject();
    jsonViewToCreate.addProperty("name", "myView");
    jsonViewToCreate.addProperty("surname", "mySurname");
    String rawViewToCreate = jsonViewToCreate.toString();
    ResponseEntity<View> rsp  = viewController.createView(authorization, rawViewToCreate);
    assertSame(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }

  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewSuccesfulTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 1;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertSame(HttpStatus.OK, rsp.getStatusCode());
  }

  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewNotAuthorizedToDeleteUserExceptionTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 3;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertSame(HttpStatus.FORBIDDEN, rsp.getStatusCode());
  }

  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewValuesNotAllowedExceptionTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 4;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertSame(HttpStatus.BAD_REQUEST, rsp.getStatusCode());
  }
}
