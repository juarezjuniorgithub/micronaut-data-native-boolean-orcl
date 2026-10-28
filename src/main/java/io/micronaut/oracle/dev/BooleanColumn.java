package io.micronaut.oracle.dev;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record BooleanColumn(String table, String column, String dataType) {
}
