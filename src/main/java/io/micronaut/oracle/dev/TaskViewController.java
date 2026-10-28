package io.micronaut.oracle.dev;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.views.ModelAndView;

import java.net.URI;
import java.util.Map;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller
@Produces(MediaType.TEXT_HTML)
public class TaskViewController {
  private static final URI UI_HOME = URI.create("/ui");
  private final BooleanDatabaseInspector databaseInspector;

  public TaskViewController(BooleanDatabaseInspector databaseInspector) {
    this.databaseInspector = databaseInspector;
  }

  @Get("/")
  public HttpResponse<?> rootRedirect() {
    return HttpResponse.seeOther(UI_HOME);
  }

  @Get("/ui")
  public ModelAndView<Map<String, Object>> restWorkbench() {
    return new ModelAndView<>("tasks/workbench", Map.<String, Object>of());
  }

  @Get("/ui/rest-workbench")
  public HttpResponse<?> legacyRestWorkbenchRedirect() {
    return HttpResponse.seeOther(UI_HOME);
  }

  @Get("/ui/database-proof")
  public ModelAndView<Map<String, Object>> databaseProof() {
    return new ModelAndView<>("tasks/database-proof",
        Map.<String, Object>of("capabilities", databaseInspector.inspect()));
  }
}
