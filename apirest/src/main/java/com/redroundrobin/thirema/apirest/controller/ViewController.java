package com.redroundrobin.thirema.apirest.controller;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/views")
public class ViewController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ViewService viewService;

  @Autowired
  public ViewController(ViewService viewService, JwtUtil jwtUtil, LogService logService,
                        UserService userService) {
    super(jwtUtil, logService, userService);
    this.viewService = viewService;
  }

  @Operation(
      summary = "Get views",
      description = "The request return a list of views",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = View.class))
              )),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })
  @GetMapping(value = {""})
  public ResponseEntity<List<View>> views(@RequestHeader("Authorization") String authorization) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtUtil.extractUsername(token));
    return ResponseEntity.ok(viewService.findAllByUser(user));
  }

  @Operation(
      summary = "Create view",
      description = "The request return the view that is been created if successful",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = View.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })
  @PostMapping(value = "")
  public ResponseEntity<View> createView(
      @RequestHeader("Authorization") String authorization,
      @RequestBody Map<String, String> rawNewView) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtUtil.extractUsername(token));
    try {
      return ResponseEntity.ok(viewService.addView(rawNewView, user));
    } catch (KeysNotFoundException | MissingFieldsException e) {
      logger.debug(e.toString());
      return new ResponseEntity(e, HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(
      summary = "Delete view",
      description = "The request is successful if the view is been deleted",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The delete is successful",
              content = @Content(
                  mediaType = "application/json"
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })
  @DeleteMapping(value = "/{viewId:.+}")
  public ResponseEntity<String> deleteView(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("viewId") int viewToDeleteId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtUtil.extractUsername(token));
    try {
      viewService.deleteView(user, viewToDeleteId);
      return ResponseEntity.ok("deleted view succesfully");
    } catch (NotAuthorizedException e) {
      logger.debug(e.toString());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    } catch (InvalidFieldsValuesException e) {
      logger.debug(e.toString());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @Operation(
      summary = "Get view",
      description = "The request return a view by view id if it is visible for the current "
          + "user",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successful",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = View.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "There is an error in the request",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "The authentication failed",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Not authorized",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Server error",
              content = @Content(
                  mediaType = "application/json",
                  examples = {
                      @ExampleObject()
                  }
              )
          )
      })
  @GetMapping(value = {"/{viewId:.+}"})
  public ResponseEntity<View> selectOneView(
      @RequestHeader("Authorization") String authorization,  @PathVariable("viewId") int viewId) {
    String token = authorization.substring(7);
    User user = userService.findByEmail(jwtUtil.extractUsername(token));
    View view = viewService.findByIdAndUserId(viewId, user.getId());
    if (view != null) {
      return ResponseEntity.ok(view);
    } else {
      logger.debug("RESPONSE STATUS: BAD_REQUEST. View " + viewId + " does not exist.");
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }
}
