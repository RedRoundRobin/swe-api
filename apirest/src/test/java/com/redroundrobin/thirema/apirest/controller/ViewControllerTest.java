package com.redroundrobin.thirema.apirest.controller;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.service.postgres.ViewService;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringRunner.class)
public class ViewControllerTest {

  @MockBean
  JwtUtil jwtTokenUtil;

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
  private String userToken = "userToken";
  private String modToken = "modToken";
  private List<View> allViews;

  @Before
  public void setUp() throws Exception {

    viewController = new ViewController(jwtTokenUtil, userService, viewService);
    Entity entity1 = new Entity();
    entity1.setId(1);

    user = new User();
    user.setId(3);
    user.setName("user1");
    user.setSurname("user1");
    user.setEmail("user1");
    user.setPassword("password");
    user.setType(User.Role.USER);
    user.setEntity(entity1);

    mod = new User();
    mod.setId(4);
    mod.setName("mod");
    mod.setSurname("mod");
    mod.setEmail("mod");
    mod.setPassword("password");
    mod.setType(User.Role.MOD);
    mod.setEntity(entity1);

    view1= new View();
    view1.setUser(user);
    view1.setViewId(1);
    view1.setName("view1");

    view2= new View();
    view2.setUser(user);
    view2.setViewId(2);
    view2.setName("view2");

    view3= new View();
    view3.setUser(mod);
    view3.setViewId(3);
    view3.setName("view3");

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
      List<View> views = allViews.stream()
        .filter(view -> view.getUser() == user)
        .collect(Collectors.toList());
      return views;
  });

    when(viewService.findByViewId(anyInt())).thenAnswer(i -> {
      int viewId = i.getArgument(0);
      Optional<View> retView = allViews.stream()
          .filter(view -> view.getViewId() == viewId)
          .findFirst();
      return retView.orElse(null);
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
      if(jsonViewToCreate.keySet().stream()
          .filter(key -> key != "name")
          .count() == 0)
        throw new MissingFieldsException("Some necessary" +
            " fields are missing: cannot create user");
      else throw new KeysNotFoundException("");
    });

      doAnswer(i -> {
      User deletingUser =  i.getArgument(0);
      int viewToDeleteId = i.getArgument(1);
      View viewToDelete = allViews.stream()
          .filter(view -> view.getViewId() == viewToDeleteId)
          .findFirst().orElse(null);

      if(viewToDelete == null) {
        throw new ValuesNotAllowedException("The given view_id" +
            " doesn't correspond to any view");
      }

      else if(viewToDelete.getUser().getId() != deletingUser.getId()) {
        throw new NotAuthorizedToDeleteUserException("This user cannot delete the view with" +
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
    assertTrue(rsp.getStatusCode() == HttpStatus.OK);
    assertTrue(!rsp.getBody().isEmpty());
  }


  //@GetMapping(value = {"/views/{viewId:.+}"}) test
  @Test
  public void selectOneViewSuccesfulTest() throws Exception{
    String authorization = "Bearer "+userToken;

    when(viewService.getViewByUserId(eq(3), eq(1))).thenAnswer(i -> {
        int viewId = i.getArgument(1);
        return viewService.findByViewId(viewId);
    });
      //attenzione a passare in questo test una view che appartine allo user sotto test!
    ResponseEntity<View> rsp = viewController.selectOneView(authorization, 1);
    assertTrue(rsp.getStatusCode() == HttpStatus.OK);
    assertTrue(rsp.getBody() != null);
  }


  //@GetMapping(value = {"/views/{viewId:.+}"}) test
  @Test
  public void selectOneViewNotFoundExceptionTest() throws Exception{
    String authorization = "Bearer "+userToken;

    when(viewService.getViewByUserId(eq(3), eq(4))).thenThrow(
        new ViewNotFoundException("")
    );

    ResponseEntity<View> rsp  = viewController.selectOneView(authorization, 4);
    assertTrue(rsp.getStatusCode() == HttpStatus.NOT_FOUND);
  }

  //@GetMapping(value = {"/views/{viewId:.+}"}) test
  @Test
  public void selectOneValuesNotAllowedExceptionTest() throws Exception{
    String authorization = "Bearer "+userToken;

    when(viewService.getViewByUserId(eq(3), eq(4))).thenThrow(
        new ValuesNotAllowedException("")
    );

    ResponseEntity<View> rsp  = viewController.selectOneView(authorization, 4);
    assertTrue(rsp.getStatusCode() == HttpStatus.BAD_REQUEST);
  }


  //@PostMapping(value = "/views/create")
  @Test
  public void createViewSuccesfulTest() {
    String authorization = "Bearer "+userToken;
    JsonObject jsonViewToCreate = new JsonObject();
    jsonViewToCreate.addProperty("name", "myView");
    String rawViewToCreate = jsonViewToCreate.toString();
    ResponseEntity<View> rsp  = viewController.createView(authorization, rawViewToCreate);
    assertTrue(rsp.getStatusCode() == HttpStatus.OK);
    assertTrue(rsp.getBody() != null);
  }

  //@PostMapping(value = "/views/create")
  @Test
  public void createViewMissingFieldsExceptionTest() {
    String authorization = "Bearer "+userToken;
    JsonObject jsonViewToCreate = new JsonObject(); // empty json
    String rawViewToCreate = jsonViewToCreate.toString();
    ResponseEntity<View> rsp  = viewController.createView(authorization, rawViewToCreate);
    assertTrue(rsp.getStatusCode() == HttpStatus.BAD_REQUEST);
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
    assertTrue(rsp.getStatusCode() == HttpStatus.BAD_REQUEST);
  }


  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewSuccesfulTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 1;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertTrue(rsp.getStatusCode() == HttpStatus.OK);

  }


  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewNotAuthorizedToDeleteUserExceptionTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 3;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertTrue(rsp.getStatusCode() == HttpStatus.FORBIDDEN);
  }


  //@DeleteMapping(value = "/views/delete/{viewId:.+}")
  @Test
  public void deleteViewValuesNotAllowedExceptionTest() {
    String authorization = "Bearer "+userToken;
    int viewToDeleteId = 4;
    ResponseEntity<?> rsp = viewController.deleteView(authorization, viewToDeleteId);
    assertTrue(rsp.getStatusCode() == HttpStatus.BAD_REQUEST);
  }
}
