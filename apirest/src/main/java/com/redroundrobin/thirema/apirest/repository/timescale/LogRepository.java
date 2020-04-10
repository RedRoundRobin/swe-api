package com.redroundrobin.thirema.apirest.repository.timescale;

import com.redroundrobin.thirema.apirest.models.timescale.Log;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends CrudRepository<Log, Timestamp> {

  @Query(value = "SELECT L FROM Log L ORDER BY L.time DESC")
  Iterable<Log> findAllOrderByTimeDesc();

  @Query(value = "SELECT * FROM logs ORDER BY time DESC LIMIT :limit",nativeQuery = true)
  Iterable<Log> findTopN(int limit);

  Iterable<Log> findAllByUserIdInOrderByTimeDesc(List<Integer> userIds);

  @Query(value = "SELECT * FROM logs WHERE user_id IN :userIds ORDER BY time DESC LIMIT :limit",
      nativeQuery = true)
  Iterable<Log> findTopNByUserIdIn(int limit, List<Integer> userIds);
}

