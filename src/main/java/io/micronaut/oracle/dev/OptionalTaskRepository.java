package io.micronaut.oracle.dev;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.ORACLE, version = "23.1")
public interface OptionalTaskRepository extends CrudRepository<OptionalTask, Long> {
  List<OptionalTask> findByCompletedTrue();

  List<OptionalTask> findByCompletedFalse();

  List<OptionalTask> findByCompletedIsNull();
}
