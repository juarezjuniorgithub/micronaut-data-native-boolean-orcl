package io.micronaut.oracle.dev;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
class OracleNativeBooleanTest {
  @Inject
  TaskRepository taskRepository;
  @Inject
  OptionalTaskRepository optionalTaskRepository;
  @Inject
  TransactionOperations<Connection> transactionOperations;
  @Inject
  @Client("/")
  HttpClient client;

  @BeforeEach
  void resetTables() {
    taskRepository.deleteAll();
    optionalTaskRepository.deleteAll();
  }

  @Test
  void createsNativeBooleanColumnsAndUsesTrueFalseAndBooleanParameterQueries() throws Exception {
    Task write = taskRepository.save(new Task(null, "Write the guide", false));
    taskRepository.save(new Task(null, "Review the guide", true));
    taskRepository.save(new Task(null, "Keep exploring", false));

    assertEquals("BOOLEAN", columnType("BOOLEAN_TASK", "COMPLETED"));
    assertEquals(List.of("Review the guide"), titles(taskRepository.findByCompletedTrue()));
    assertContainsExactly(titles(taskRepository.findByCompletedFalse()), "Write the guide", "Keep exploring");
    assertEquals(List.of("Review the guide"), titles(taskRepository.findByCompleted(true)));
    assertEquals(1, taskRepository.updateCompleted(write.id(), true));
    assertContainsExactly(titles(taskRepository.findByCompletedTrue()), "Write the guide", "Review the guide");
  }

  @Test
  void nullableWrapperMapsToOracleThreeValuedBooleanLogic() throws Exception {
    optionalTaskRepository.save(new OptionalTask(null, "Unknown", null));
    optionalTaskRepository.save(new OptionalTask(null, "False", false));
    optionalTaskRepository.save(new OptionalTask(null, "True", true));

    assertEquals("BOOLEAN", columnType("OPTIONAL_BOOLEAN_TASK", "COMPLETED"));
    assertEquals(List.of("True"),
        optionalTaskRepository.findByCompletedTrue().stream().map(OptionalTask::title).toList());
    assertEquals(List.of("False"),
        optionalTaskRepository.findByCompletedFalse().stream().map(OptionalTask::title).toList());
    assertEquals(List.of("Unknown"),
        optionalTaskRepository.findByCompletedIsNull().stream().map(OptionalTask::title).toList());
  }

  @Test
  void restApiCreatesFiltersAndUpdatesABoolean() {
    Task created = client.toBlocking()
        .retrieve(HttpRequest.POST("/api/tasks", new CreateTaskRequest("Use native BOOLEAN", false)), Task.class);

    List<Task> open = client.toBlocking().retrieve(HttpRequest.GET("/api/tasks/open"), Argument.listOf(Task.class));
    assertEquals(List.of("Use native BOOLEAN"), titles(open));

    assertEquals(HttpStatus.NO_CONTENT,
        client.toBlocking()
            .exchange(HttpRequest.PUT("/api/tasks/" + created.id() + "/completed", new UpdateCompletedRequest(true)))
            .getStatus());
    List<Task> completed = client.toBlocking().retrieve(HttpRequest.GET("/api/tasks/state/true"),
        Argument.listOf(Task.class));
    assertEquals(List.of("Use native BOOLEAN"), titles(completed));

    HttpClientResponseException exception = assertThrows(HttpClientResponseException.class,
        () -> client.toBlocking().exchange(HttpRequest.GET("/api/tasks/999999")));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
  }

  private String columnType(String table, String column) {
    return transactionOperations.executeRead(status -> {
      try (PreparedStatement statement = status.getConnection()
          .prepareStatement("SELECT data_type FROM user_tab_columns WHERE table_name = ? AND column_name = ?")) {
        statement.setString(1, table);
        statement.setString(2, column);
        try (ResultSet result = statement.executeQuery()) {
          if (!result.next()) {
            throw new AssertionError("Expected metadata row for " + table + "." + column);
          }
          return result.getString(1);
        }
      } catch (java.sql.SQLException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  private static List<String> titles(List<Task> tasks) {
    return tasks.stream().map(Task::title).toList();
  }

  private static void assertContainsExactly(List<String> actual, String... expected) {
    assertEquals(expected.length, actual.size());
    for (String title : expected) {
      assertTrue(actual.contains(title), () -> "Expected title: " + title + ", actual: " + actual);
    }
  }
}
