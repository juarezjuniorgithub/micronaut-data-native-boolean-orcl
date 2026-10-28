package io.micronaut.oracle.dev;

import jakarta.inject.Singleton;
import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.tracing.annotation.NewSpan;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Singleton
class BooleanDatabaseInspector {
  private static final String COLUMN_TYPE_SQL = """
      SELECT data_type
      FROM user_tab_columns
      WHERE table_name = ? AND column_name = ?
      """;

  private final DataSource dataSource;
  private final NativeBooleanObservability observability;

  BooleanDatabaseInspector(DataSource dataSource, NativeBooleanObservability observability) {
    this.dataSource = dataSource;
    this.observability = observability;
  }

  @Transactional(readOnly = true)
  @NewSpan("oracle.native_boolean.inspect_metadata")
  BooleanCapabilities inspect() {
    observability.metadataInspectionStarted();
    try (Connection connection = dataSource.getConnection()) {
      DatabaseMetaData metadata = connection.getMetaData();
      BooleanCapabilities capabilities = new BooleanCapabilities(metadata.getDatabaseProductName(),
          metadata.getDatabaseProductVersion(), metadata.getDriverName(), metadata.getDriverVersion(),
          List.of(new BooleanColumn("BOOLEAN_TASK", "COMPLETED", columnType(connection, "BOOLEAN_TASK", "COMPLETED")),
              new BooleanColumn("OPTIONAL_BOOLEAN_TASK", "COMPLETED",
                  columnType(connection, "OPTIONAL_BOOLEAN_TASK", "COMPLETED"))));
      capabilities.booleanColumns().forEach(observability::metadataColumn);
      observability.metadataInspectionCompleted(capabilities);
      return capabilities;
    } catch (SQLException e) {
      observability.metadataInspectionFailed(e);
      throw new IllegalStateException("Unable to inspect Oracle BOOLEAN metadata", e);
    }
  }

  private String columnType(Connection connection, String table, String column) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(COLUMN_TYPE_SQL)) {
      statement.setString(1, table);
      statement.setString(2, column);
      try (ResultSet result = statement.executeQuery()) {
        return result.next() ? result.getString(1) : "NOT_FOUND";
      }
    }
  }
}
