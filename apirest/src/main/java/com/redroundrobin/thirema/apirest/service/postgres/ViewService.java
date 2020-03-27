package com.redroundrobin.thirema.apirest.service.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.View;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

  @Autowired
  private ViewRepository repository;

  public List<View> findByUserId(int userId){ return repository.findByUserId(userId); }

  public View findByViewId(int viewId){ return repository.findByViewId(viewId); }
}
