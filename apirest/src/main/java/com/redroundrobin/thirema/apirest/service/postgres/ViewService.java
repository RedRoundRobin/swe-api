package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedToDeleteUserException;
import com.redroundrobin.thirema.apirest.utils.exception.ValuesNotAllowedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

  private ViewRepository viewRepo;

  private UserService userService;

  private boolean checkCreatableFields(Set<String> keys)
      throws KeysNotFoundException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");

    boolean onlyCreatableKeys = keys.stream()
        .filter(key -> !creatable.contains(key))
        .count() == 0;

    if (!onlyCreatableKeys) {
      throw new KeysNotFoundException("There are some keys that either"
          + " do not exist or you are not allowed to edit them");
    }

    return creatable.size() == keys.size();
  }

  @Autowired
  public ViewService(ViewRepository viewRepo) {
    this.viewRepo = viewRepo;
  }

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public List<View> findAllByUser(User user) {
    return viewRepo.findAllByUser(user);
  }

  public View findById(int viewId) {
    return viewRepo.findById(viewId).orElse(null);
  }

  public View findByIdAndUserId(int viewId, int userId) {
    User user = userService.findById(userId);
    if (user != null) {
      return viewRepo.findByViewIdAndUser(viewId, user);
    } else {
      return null;
    }
  }

  public View serializeView(JsonObject rawViewToInsert, User insertingUser)
      throws KeysNotFoundException, MissingFieldsException {
    if (!checkCreatableFields(rawViewToInsert.keySet())) {
      throw new MissingFieldsException("Some necessary fields are missing: cannot create view");
    }

    View newView = new View();
    newView.setName(rawViewToInsert.get("name").getAsString());
    newView.setUser(insertingUser);
    return viewRepo.save(newView);
  }

  public void deleteView(User deletingUser, int viewToDeleteId)
      throws NotAuthorizedToDeleteUserException, ValuesNotAllowedException {
    View viewToDelete;
    if ((viewToDelete = viewRepo.findById(viewToDeleteId).orElse(null)) == null) {
      throw new ValuesNotAllowedException("The given view_id doesn't correspond to any view");
    }

    if (viewToDelete.getUser().getId() != deletingUser.getId()) {
      throw new NotAuthorizedToDeleteUserException("This user cannot delete the view with"
          + "the view_id given");
    }

    viewRepo.delete(viewToDelete);
  }
}
