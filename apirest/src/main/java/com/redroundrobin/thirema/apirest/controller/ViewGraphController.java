package com.redroundrobin.thirema.apirest.controller;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.service.postgres.UserService;
import com.redroundrobin.thirema.apirest.service.postgres.ViewGraphService;
import com.redroundrobin.thirema.apirest.service.timescale.LogService;
import com.redroundrobin.thirema.apirest.utils.JwtUtil;
import com.redroundrobin.thirema.apirest.utils.exception.ElementNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/viewGraphs"})
public class ViewGraphController extends CoreController {

  protected Logger logger = LoggerFactory.getLogger(this.getClass());

  private final ViewGraphService viewGraphService;

  @Autowired
  public ViewGraphController(ViewGraphService viewGraphService, JwtUtil jwtUtil,
                             LogService logService, UserService userService) {
    super(jwtUtil, logService, userService);
    this.viewGraphService = viewGraphService;
  }

  @Operation(
      summary = "Get viewGraphs",
      description = "The request return a list of viewGraphs",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = ViewGraph.class))
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
  public ResponseEntity<List<ViewGraph>> getViewGraphs(
      @RequestHeader("authorization") String authorization,
      @RequestParam(value = "userId", required = false) Integer userId,
      @RequestParam(value = "viewId", required = false) Integer viewId) {
    User user = this.getUserFromAuthorization(authorization);
    if (user.getType() == User.Role.ADMIN) {
      if (userId != null && viewId != null) {
        return ResponseEntity.ok(viewGraphService.findAllByUserIdAndViewId(userId,viewId));
      } else if (userId != null) {
        return ResponseEntity.ok(viewGraphService.findAllByUserId(userId));
      } else if (viewId != null) {
        return ResponseEntity.ok(viewGraphService.findAllByViewId(viewId));
      } else {
        return ResponseEntity.ok(viewGraphService.findAll());
      }
    } else if (viewId == null && (userId == null || user.getId() == userId)) {
      return ResponseEntity.ok(viewGraphService.findAllByUserId(user.getId()));
    } else if (viewId != null && (userId == null || user.getId() == userId)) {
      return ResponseEntity.ok(viewGraphService.findAllByUserIdAndViewId(user.getId(),viewId));
    } else {
      return ResponseEntity.ok(Collections.emptyList());
    }
  }

  @Operation(
      summary = "Get viewGraphs",
      description = "The request return a viewGraph by id if it is visible for the current user",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ViewGraph.class)
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
  @GetMapping(value = {"/{viewGraphId:.+}"})
  public ResponseEntity<ViewGraph> getViewGraph(
      @RequestHeader("authorization") String authorization,
      @PathVariable("viewGraphId") int viewGraphId) {
    User user = this.getUserFromAuthorization(authorization);
    try {
      if (user.getType() == User.Role.ADMIN
          || viewGraphService.getPermissionByIdAndUserId(viewGraphId, user.getId())) {
        return ResponseEntity.ok(viewGraphService.findById(viewGraphId));
      } else {
        logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin and is not a "
            + "moderator of the user who have the viewGraph or is not the one who have the "
            + "viewGraph");
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    } catch (ElementNotFoundException nfe) {
      logger.debug(nfe.toString());
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(
      summary = "Create viewGraph",
      description = "The request return the viewGraph that is been created if successfull",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ViewGraph.class)
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
  @PostMapping(value = {""})
  public ResponseEntity<ViewGraph> createUserViewGraphs(
      @RequestHeader("authorization") String authorization,
      @RequestBody Map<String, Integer> newViewGraphFields) {
    User user = this.getUserFromAuthorization(authorization);
    try {
      return ResponseEntity.ok(viewGraphService.addViewGraph(user, newViewGraphFields));
    } catch (MissingFieldsException | InvalidFieldsValuesException fe) {
      logger.debug(fe.toString());
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(
      summary = "Edit viewGraph",
      description = "The request return the viewGraph that is been edited if successfull",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The request is successfull",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ViewGraph.class)
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
  @PutMapping(value = {"/{viewGraphId:.+}"})
  public ResponseEntity<ViewGraph> editViewGraph(
      @RequestHeader("authorization") String authorization,
      @PathVariable("viewGraphId") int viewGraphId,
      @RequestBody Map<String, Integer> newViewGraphFields) {
    User user = this.getUserFromAuthorization(authorization);
    try {
      if (user.getType() == User.Role.ADMIN
          || viewGraphService.getPermissionByIdAndUserId(viewGraphId, user.getId())) {
        return ResponseEntity.ok(viewGraphService.editViewGraph(user, viewGraphId,
            newViewGraphFields));
      } else {
        logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin and is not a "
            + "moderator of the user who have the viewGraph or is not the one who have the "
            + "viewGraph");
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    } catch (ElementNotFoundException | MissingFieldsException | InvalidFieldsValuesException fe) {
      logger.debug(fe.toString());
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  @Operation(
      summary = "Delete viewGraph",
      description = "The request is successfull if the viewGraph is been deleted",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "The delete is successfull",
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
  @DeleteMapping(value = {"/{viewGraphId:.+}"})
  public ResponseEntity deleteUserViewGraph(
      @RequestHeader("authorization") String authorization,
      @PathVariable("viewGraphId") int viewGraphId) {
    User user = this.getUserFromAuthorization(authorization);
    try {
      if (user.getType() == User.Role.ADMIN
          || viewGraphService.getPermissionByIdAndUserId(viewGraphId, user.getId())) {
        if (viewGraphService.deleteViewGraph(viewGraphId)) {
          return new ResponseEntity(HttpStatus.OK);
        } else {
          logger.debug("RESPONSE STATUS: CONFLICT. There was a db error during the deletion of "
              + "the viewGraph");
          return new ResponseEntity(HttpStatus.CONFLICT);
        }
      } else {
        logger.debug("RESPONSE STATUS: FORBIDDEN. User is not an admin and is not a "
            + "moderator of the user who have the viewGraph or is not the one who have the "
            + "viewGraph");
        return new ResponseEntity(HttpStatus.FORBIDDEN);
      }
    } catch (ElementNotFoundException nfe) {
      logger.debug(nfe.toString());
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }
}
