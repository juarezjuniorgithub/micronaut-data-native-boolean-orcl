package io.micronaut.oracle.dev;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@MappedEntity("OPTIONAL_BOOLEAN_TASK")
public record OptionalTask(@Id @GeneratedValue @Nullable Long id, String title, @Nullable Boolean completed) {
}
