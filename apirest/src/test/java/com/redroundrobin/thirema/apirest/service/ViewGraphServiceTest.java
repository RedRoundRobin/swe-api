package com.redroundrobin.thirema.apirest.service;

import com.redroundrobin.thirema.apirest.repository.postgres.ViewGraphRepository;
import com.redroundrobin.thirema.apirest.service.ViewGraphService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ViewGraphServiceTest {

  @MockBean
  private ViewGraphRepository viewGraphRepo;

  private ViewGraphService viewGraphService;

  @Before
  public void setUp() {
    this.viewGraphService = new ViewGraphService(viewGraphRepo);
  }
}
