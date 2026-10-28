package io.micronaut.oracle.dev;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UpdateCompletedRequest(boolean completed) {
}
