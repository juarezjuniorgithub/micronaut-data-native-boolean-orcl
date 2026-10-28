package io.micronaut.oracle.dev;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record BooleanCapabilities(String databaseProduct, String databaseVersion, String jdbcDriver,
    String jdbcDriverVersion, List<BooleanColumn> booleanColumns) {
}
