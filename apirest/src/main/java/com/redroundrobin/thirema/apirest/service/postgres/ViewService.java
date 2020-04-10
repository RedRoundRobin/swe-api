package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import com.redroundrobin.thirema.apirest.utils.exception.InvalidFieldsValuesException;
import com.redroundrobin.thirema.apirest.utils.exception.KeysNotFoundException;
import com.redroundrobin.thirema.apirest.utils.exception.MissingFieldsException;
import com.redroundrobin.thirema.apirest.utils.exception.NotAuthorizedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

  private ViewRepository viewRepo;

  private UserRepository userRepo;

  @Autowired
  public ViewService(ViewRepository viewRepository, UserRepository userRepository) {
    this.viewRepo = viewRepository;
    this.userRepo = userRepository;
  }

  private boolean checkCreatableFields(Set<String> keys)
      throws KeysNotFoundException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");

    boolean onlyCreatableKeys = creatable.containsAll(keys);

    if (!onlyCreatableKeys) {
      throw new KeysNotFoundException("There are some keys that either"
          + " do not exist or you are not allowed to edit them");
    }

    return creatable.size() == keys.size();
  }

  public void deleteView(User deletingUser, int viewToDeleteId)
      throws NotAuthorizedException, InvalidFieldsValuesException {
    View viewToDelete;
    if ((viewToDelete = viewRepo.findById(viewToDeleteId).orElse(null)) == null) {
      throw new InvalidFieldsValuesException("The given view_id doesn't correspond to any view");
    }

    if (viewToDelete.getUser().getId() != deletingUser.getId()) {
      throw new NotAuthorizedException("This user cannot delete the view with"
          + "the view_id given");
    }

    viewRepo.delete(viewToDelete);
  }

  public List<View> findAllByUser(User user) {
    return (List<View>) viewRepo.findAllByUser(user);
  }

  public View findById(int id) {
    return viewRepo.findById(id).orElse(null);
  }

  public View findByIdAndUserId(int id, int userId) {
    User user = userRepo.findById(userId).orElse(null);
    if (user != null) {
      return viewRepo.findByViewIdAndUser(id, user);
    } else {
      return null;
    }
  }

  public View serializeView(JsonObject rawViewToInsert, User insertingUser)
      throws KeysNotFoundException, MissingFieldsException {
    if (!checkCreatableFields(rawViewToInsert.keySet())) {
      throw new MissingFieldsException("Some necessary fields are missing: cannot create view");
    }

    View newView = new View(rawViewToInsert.get("name").getAsString(), insertingUser);
    return viewRepo.save(newView);
  }

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepo = userRepository;
  }

}
