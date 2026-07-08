package com.kjs.wuli3.core.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Supplies the application clock so time-dependent code can avoid hard-coded system time.
 */
@FunctionalInterface
public interface ClockProvider {

    Clock clock();

    default Instant instant() {
        return this.clock().instant();
    }

    default ZoneId zone() {
        return this.clock().getZone();
    }

    static ClockProvider system(final ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return () -> Clock.system(zone);
    }

    static ClockProvider fixed(final Instant instant, final ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return () -> Clock.fixed(instant, zone);
    }
}
