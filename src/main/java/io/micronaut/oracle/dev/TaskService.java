package io.micronaut.oracle.dev;

import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
class TaskService {
  private final TaskRepository taskRepository;
  private final NativeBooleanObservability observability;

  TaskService(TaskRepository taskRepository, NativeBooleanObservability observability) {
    this.taskRepository = taskRepository;
    this.observability = observability;
  }

  @NewSpan("oracle.native_boolean.read_all")
  List<Task> all() {
    List<Task> tasks = taskRepository.findAll();
    observability.tasksRead("findAll", Optional.empty(), tasks.size());
    return tasks;
  }

  @NewSpan("oracle.native_boolean.read_true")
  List<Task> completed() {
    List<Task> tasks = taskRepository.findByCompletedTrue();
    observability.tasksRead("findByCompletedTrue", Optional.of(true), tasks.size());
    return tasks;
  }

  @NewSpan("oracle.native_boolean.read_false")
  List<Task> open() {
    List<Task> tasks = taskRepository.findByCompletedFalse();
    observability.tasksRead("findByCompletedFalse", Optional.of(false), tasks.size());
    return tasks;
  }

  @NewSpan("oracle.native_boolean.read_parameterized")
  List<Task> byCompleted(@SpanTag("oracle.boolean.completed") boolean completed) {
    List<Task> tasks = taskRepository.findByCompleted(completed);
    observability.tasksRead("findByCompleted", Optional.of(completed), tasks.size());
    return tasks;
  }

  @NewSpan("oracle.native_boolean.find_by_id")
  Optional<Task> findById(Long id) {
    Optional<Task> task = taskRepository.findById(id);
    observability.taskLookup(id, task.isPresent());
    return task;
  }

  @Transactional
  @NewSpan("oracle.native_boolean.insert")
  Task create(CreateTaskRequest request) {
    Task task = taskRepository.save(new Task(null, request.title().trim(), request.completed()));
    observability.taskCreated(task, request.title().trim().length());
    return task;
  }

  @Transactional
  @NewSpan("oracle.native_boolean.update")
  boolean updateCompleted(Long id, @SpanTag("oracle.boolean.completed") boolean completed) {
    boolean updated = taskRepository.updateCompleted(id, completed) == 1;
    observability.taskCompletionUpdated(id, completed, updated);
    return updated;
  }
}
