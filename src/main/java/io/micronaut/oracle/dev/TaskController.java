package io.micronaut.oracle.dev;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import java.util.List;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/api")
public class TaskController {
  private final TaskService taskService;
  private final BooleanDatabaseInspector databaseInspector;
  private final NativeBooleanObservability observability;

  public TaskController(TaskService taskService, BooleanDatabaseInspector databaseInspector,
      NativeBooleanObservability observability) {
    this.taskService = taskService;
    this.databaseInspector = databaseInspector;
    this.observability = observability;
  }

  @Get("/tasks")
  @NewSpan("oracle.native_boolean.endpoint.read_all")
  public List<Task> all() {
    observability.booleanEndpointSelected("GET /api/tasks");
    return taskService.all();
  }

  @Get("/tasks/completed")
  @NewSpan("oracle.native_boolean.endpoint.read_true")
  public List<Task> completed() {
    observability.booleanEndpointSelected("GET /api/tasks/completed");
    return taskService.completed();
  }

  @Get("/tasks/open")
  @NewSpan("oracle.native_boolean.endpoint.read_false")
  public List<Task> open() {
    observability.booleanEndpointSelected("GET /api/tasks/open");
    return taskService.open();
  }

  @Get("/tasks/state/{completed}")
  @NewSpan("oracle.native_boolean.endpoint.read_parameterized")
  public List<Task> byBooleanParameter(@PathVariable boolean completed) {
    observability.booleanEndpointSelected("GET /api/tasks/state/{completed}");
    return taskService.byCompleted(completed);
  }

  @Get("/tasks/{id}")
  @NewSpan("oracle.native_boolean.endpoint.find_by_id")
  public HttpResponse<Task> findById(@PathVariable Long id) {
    observability.booleanEndpointSelected("GET /api/tasks/{id}");
    return taskService.findById(id).map(HttpResponse::ok).orElseGet(HttpResponse::notFound);
  }

  @Post("/tasks")
  @NewSpan("oracle.native_boolean.endpoint.insert")
  public HttpResponse<Task> create(@Valid @Body CreateTaskRequest request) {
    observability.booleanEndpointSelected("POST /api/tasks");
    return HttpResponse.created(taskService.create(request));
  }

  @Put("/tasks/{id}/completed")
  @NewSpan("oracle.native_boolean.endpoint.update")
  public HttpResponse<?> updateCompleted(@PathVariable Long id, @Body UpdateCompletedRequest request) {
    observability.booleanEndpointSelected("PUT /api/tasks/{id}/completed");
    return taskService.updateCompleted(id, request.completed()) ? HttpResponse.noContent() : HttpResponse.notFound();
  }

  @Get("/boolean/capabilities")
  @NewSpan("oracle.native_boolean.endpoint.inspect_metadata")
  public BooleanCapabilities capabilities() {
    observability.booleanEndpointSelected("GET /api/boolean/capabilities");
    return databaseInspector.inspect();
  }
}
