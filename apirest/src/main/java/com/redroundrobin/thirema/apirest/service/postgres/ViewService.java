package com.redroundrobin.thirema.apirest.service.postgres;

import com.google.gson.JsonObject;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redroundrobin.thirema.apirest.utils.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

  private ViewRepository viewRepo;


  private boolean checkCreatableFields(Set<String> keys)
      throws KeysNotFoundException {
    Set<String> creatable = new HashSet<>();
    creatable.add("name");

    boolean onlyCreatableKeys = keys.stream()
        .filter(key -> !creatable.contains(key))
        .count() == 0;

    if (!onlyCreatableKeys)
      throw new KeysNotFoundException("There are some keys that either do not exist or you are not" +
          "allowed to edit them");

    return creatable.size() == keys.size();
  }

  @Autowired
  public ViewService(ViewRepository viewRepo) {
    this.viewRepo = viewRepo;
  }


  public List<View> findAllByUser(User user) {
    return viewRepo.findAllByUserId(user);
  }

  public View findByViewId(int viewId){
    return viewRepo.findByViewId(viewId);
  }

  public View getViewByUserId(int userId, int viewId) throws
      ViewNotFoundException, ValuesNotAllowedException {
    View view = viewRepo.findByViewId(viewId);
    if(view != null && userId == view.getUser().getId())
      return view;
    if(!(view != null))
      throw new ViewNotFoundException("");
    throw new ValuesNotAllowedException("");

  }

  public View serializeView(JsonObject rawViewToInsert, User insertingUser)
      throws KeysNotFoundException, MissingFieldsException {
    if(!checkCreatableFields(rawViewToInsert.keySet())) {
      throw new MissingFieldsException("Some necessary fields are missing: cannot create user");
    }

    View newView = new View();
    newView.setName(rawViewToInsert.get("name").getAsString());
    newView.setUser(insertingUser);
    return viewRepo.save(newView);
  }

  public void deleteView(User deletingUser, int viewToDeleteId)
      throws NotAuthorizedToDeleteUserException, ValuesNotAllowedException {
    View viewToDelete;
    if((viewToDelete = viewRepo.findByViewId(viewToDeleteId)) == null) {
      throw new ValuesNotAllowedException("The given view_id doesn't correspond to any view");
    }

    if(viewToDelete.getUser().getId() != deletingUser.getId())
      throw new NotAuthorizedToDeleteUserException("This user cannot delete the view with" +
          "the view_id given");

    viewRepo.delete(viewToDelete);
  }
}
