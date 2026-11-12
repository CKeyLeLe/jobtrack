package com.jobtrack.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * PostgreSQL stocke les timestamps à la microseconde, alors que {@link Instant#now()} peut
 * en avoir la nanoseconde sous Windows. Sans troncature, la valeur renvoyée juste après
 * un INSERT diffère de celle relue ensuite en base.
 */
public final class Timestamps {

    private Timestamps() {
    }

    public static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}
