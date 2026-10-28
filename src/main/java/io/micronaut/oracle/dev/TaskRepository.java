package io.micronaut.oracle.dev;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.ORACLE, version = "23.1")
public interface TaskRepository extends CrudRepository<Task, Long> {
  List<Task> findByCompletedTrue();

  List<Task> findByCompletedFalse();

  List<Task> findByCompleted(boolean completed);

  long updateCompleted(@Id Long id, boolean completed);
}
