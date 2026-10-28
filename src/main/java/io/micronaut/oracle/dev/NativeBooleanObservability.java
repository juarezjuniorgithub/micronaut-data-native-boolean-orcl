package io.micronaut.oracle.dev;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
final class NativeBooleanObservability {
  private static final Logger LOG = LoggerFactory.getLogger(NativeBooleanObservability.class);

  void booleanEndpointSelected(String operation) {
    log("oracle.boolean.endpoint", "operation={}", operation);
  }

  void tasksRead(String operation, Optional<Boolean> completed, int resultCount) {
    String completionFilter = completed.map(String::valueOf).orElse("not-applied");
    log("oracle.boolean.read", "operation={} completedFilter={} resultCount={} mapping=java.boolean-to-oracle.BOOLEAN",
        operation, completionFilter, resultCount);
  }

  void taskLookup(Long id, boolean found) {
    log("oracle.boolean.lookup", "id={} found={} mapping=java.boolean-to-oracle.BOOLEAN", id, found);
  }

  void taskCreated(Task task, int titleLength) {
    log("oracle.boolean.insert", "id={} completed={} titleLength={} mapping=java.boolean-to-oracle.BOOLEAN", task.id(),
        task.completed(), titleLength);
  }

  void taskCompletionUpdated(Long id, boolean completed, boolean updated) {
    log("oracle.boolean.update", "id={} completed={} updated={} mapping=java.boolean-to-oracle.BOOLEAN", id, completed,
        updated);
  }

  void metadataInspectionStarted() {
    log("oracle.boolean.metadata.inspect", "source=USER_TAB_COLUMNS query=parameterized");
  }

  void metadataColumn(BooleanColumn column) {
    log("oracle.boolean.metadata.column", "table={} column={} oracleType={}", column.table(), column.column(),
        column.dataType());
  }

  void metadataInspectionCompleted(BooleanCapabilities capabilities) {
    log("oracle.boolean.metadata.complete", "databaseProduct={} jdbcDriver={} mappedBooleanColumns={}",
        capabilities.databaseProduct(), capabilities.jdbcDriver(), capabilities.booleanColumns().size());
  }

  void metadataInspectionFailed(Exception exception) {
    TraceIds traceIds = traceIds();
    LOG.error("event=oracle.boolean.metadata.failure exceptionType={} traceId={} spanId={}",
        exception.getClass().getSimpleName(), traceIds.traceId(), traceIds.spanId(), exception);
  }

  private void log(String event, String details, Object... arguments) {
    TraceIds traceIds = traceIds();
    Object[] values = new Object[arguments.length + 3];
    values[0] = event;
    System.arraycopy(arguments, 0, values, 1, arguments.length);
    values[arguments.length + 1] = traceIds.traceId();
    values[arguments.length + 2] = traceIds.spanId();
    LOG.info("event={} " + details + " traceId={} spanId={}", values);
  }

  private TraceIds traceIds() {
    SpanContext context = Span.current().getSpanContext();
    return context.isValid() ? new TraceIds(context.getTraceId(), context.getSpanId())
        : new TraceIds("not-active", "not-active");
  }

  private record TraceIds(String traceId, String spanId) {
  }
}
