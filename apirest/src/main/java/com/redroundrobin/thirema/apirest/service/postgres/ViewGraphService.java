package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.models.postgres.ViewGraph;
import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ViewGraphService {
  private ViewGraphRepository viewGraphRepo;

  @Autowired
  public ViewGraphService(ViewGraphRepository viewGraphRepo) {
    this.viewGraphRepo = viewGraphRepo;
  }

  public ViewGraph find(int id) {
    Optional<ViewGraph> optUser = viewGraphRepo.findById(id);
    return optUser.orElse(null);
  }

  public List<ViewGraph> findAll() {
    return (List<ViewGraph>) viewGraphRepo.findAll();
  }
}
