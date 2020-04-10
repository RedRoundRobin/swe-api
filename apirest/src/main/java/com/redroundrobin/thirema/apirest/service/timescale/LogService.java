package com.redroundrobin.thirema.apirest.service.timescale;

import com.redroundrobin.thirema.apirest.models.postgres.Entity;
import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.timescale.Log;
import com.redroundrobin.thirema.apirest.repository.postgres.EntityRepository;
import com.redroundrobin.thirema.apirest.repository.postgres.UserRepository;
import com.redroundrobin.thirema.apirest.repository.timescale.LogRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

  private final EntityRepository entityRepo;

  private final LogRepository logRepo;

  private final UserRepository userRepo;

  private List<Log> findAllByOptEntityId(Integer entityId) {
    List<Integer> userIds;
    if (entityId != null) {
      Entity entity = entityRepo.findById(entityId).orElse(null);
      userIds = ((List<User>) userRepo.findAllByEntity(entity)).stream()
          .map(User::getId)
          .collect(Collectors.toList());

      return (List<Log>) logRepo.findAllByUserIdInOrderByTimeDesc(userIds);
    } else {
      return (List<Log>) logRepo.findAllOrderByTimeDesc();
    }
  }

  private List<Log> findTopNByOptEntityId(Integer limit, Integer entityId) {
    List<Integer> userIds;
    if (entityId != null) {
      Entity entity = entityRepo.findById(entityId).orElse(null);
      userIds = ((List<User>) userRepo.findAllByEntity(entity)).stream()
          .map(User::getId)
          .collect(Collectors.toList());

      return (List<Log>) logRepo.findTopNByUserIdIn(limit, userIds);
    } else {
      return (List<Log>) logRepo.findTopN(limit);
    }
  }

  @Autowired
  public LogService(EntityRepository entityRepository, LogRepository logRepository,
                    UserRepository userRepository) {
    this.entityRepo = entityRepository;
    this.logRepo = logRepository;
    this.userRepo = userRepository;
  }

  public List<Log> findAll() {
    return findAllByOptEntityId(null);
  }

  public List<Log> findAllByEntityId(int entityId) {
    return findAllByOptEntityId(entityId);
  }

  public List<Log> findTopN(int limit) {
    return findTopNByOptEntityId(limit, null);
  }

  public List<Log> findTopNByEntityId(int limit, int entityId) {
    return findTopNByOptEntityId(limit, entityId);
  }

  public void createLog(int userId, String ip, String operation, String data) {
    Log newLog = new Log(userId, ip != null ? ip : "unknown", operation, data);

    logRepo.save(newLog);
  }
}
