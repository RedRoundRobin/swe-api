package com.redroundrobin.thirema.apirest.repository.postgres;

import com.redroundrobin.thirema.apirest.models.postgres.User;
import com.redroundrobin.thirema.apirest.models.postgres.View;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ViewRepository extends CrudRepository<View, Integer> {
  Iterable<View> findAllByUser(User user);

  View findByViewIdAndUser(int viewId, User user);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM View V WHERE V.user.userId = :id")
  void deleteAllByUserId(int id);
}
