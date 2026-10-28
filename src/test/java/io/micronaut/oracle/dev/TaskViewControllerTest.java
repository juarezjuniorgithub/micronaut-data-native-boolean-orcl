package io.micronaut.oracle.dev;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
class TaskViewControllerTest {
  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void rootAndLegacyWorkbenchRouteRedirectToUiLandingPage() {
    HttpResponse<?> rootResponse = client.toBlocking().exchange(HttpRequest.GET("/"));
    HttpResponse<?> legacyWorkbenchResponse = client.toBlocking().exchange(HttpRequest.GET("/ui/rest-workbench"));

    assertEquals(HttpStatus.SEE_OTHER, rootResponse.getStatus());
    assertEquals("/ui", rootResponse.getHeaders().get("Location"));
    assertEquals(HttpStatus.SEE_OTHER, legacyWorkbenchResponse.getStatus());
    assertEquals("/ui", legacyWorkbenchResponse.getHeaders().get("Location"));
  }

  @Test
  void restWorkbenchSeparatesCreateAndUpdateOperations() {
    String html = client.toBlocking().retrieve(HttpRequest.GET("/ui"));
    String normalizedHtml = html.replaceAll("\\s+", " ");

    assertTrue(html.contains("Oracle BOOLEAN Data Type Lab"));
    assertTrue(html.contains("class=\"hero-title\""));
    assertTrue(html.contains("class=\"site-header\""));
    assertTrue(html.contains("class=\"architecture-card\""));
    assertTrue(html.contains("href=\"/ui\""));
    assertTrue(html.contains("href=\"/ui/database-proof\""));
    assertFalse(html.contains(">Tasks</a>"));
    assertTrue(html.contains("id=\"api-create-form\""));
    assertTrue(html.contains("id=\"api-update-form\""));
    assertTrue(html.contains("id=\"api-result-status\""));
    assertTrue(html.contains("id=\"task-response-rows\""));
    assertTrue(html.contains("Task attributes"));
    assertTrue(html.contains("Create a task"));
    assertTrue(html.contains("Update a task"));
    assertFalse(html.contains("action=\"/ui/tasks\""));
    assertTrue(normalizedHtml.contains("GET /api/boolean/capabilities"));
  }

  @Test
  void databaseProofIsTheSecondFocusedPage() {
    String html = client.toBlocking().retrieve(HttpRequest.GET("/ui/database-proof"));
    String normalizedHtml = html.replaceAll("\\s+", " ");

    assertTrue(html.contains("Native type verification"));
    assertTrue(html.contains("BOOLEAN_TASK"));
    assertTrue(html.contains("compact-metadata-table"));
    assertTrue(normalizedHtml.contains("Use REST workbench"));
    assertFalse(html.contains("id=\"api-create-form\""));
    assertFalse(html.contains(">Tasks</a>"));
  }

  @Test
  void pageAssetsAreServedFromTheStandardStaticLocation() {
    String css = client.toBlocking().retrieve(HttpRequest.GET("/css/micronaut.css"));
    String javascript = client.toBlocking().retrieve(HttpRequest.GET("/js/rest-workbench.js"));

    assertTrue(css.contains("--mn-navy"));
    assertTrue(css.contains("architecture-card"));
    assertTrue(css.contains("h1.hero-title"));
    assertTrue(css.contains("request-response"));
    assertTrue(css.contains("compact-metadata-table"));
    assertTrue(javascript.contains("api-create-form"));
    assertTrue(javascript.contains("api-update-form"));
    assertTrue(javascript.contains("api-result-status"));
    assertTrue(javascript.contains("task-response-rows"));
    assertTrue(javascript.contains("renderTaskTable"));
    assertTrue(javascript.contains("Object.hasOwn"));
    assertTrue(javascript.contains("Loading ${requestLabel}"));
    assertTrue(javascript.contains("textContent"));
  }
}
