package io.micronaut.oracle.dev;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public record CreateTaskRequest(@NotBlank @Size(max = 255) String title, boolean completed) {
}
