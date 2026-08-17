package com.kjs.wuli3.core.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 提供应用时钟，避免时间相关代码直接依赖系统时间。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface ClockProvider {

    /** 返回应用使用的时钟。 */
    Clock clock();

    /** 返回当前时钟提供的时间点。 */
    default Instant instant() {
        return this.clock().instant();
    }

    /** 返回当前时钟的时区。 */
    default ZoneId zone() {
        return this.clock().getZone();
    }

    /**
     * 创建指定时区的系统时钟提供器。
     *
     * @param zone 时区
     * @return 系统时钟提供器
     */
    static ClockProvider system(final ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return () -> Clock.system(zone);
    }

    /**
     * 创建固定时间点的时钟提供器。
     *
     * @param instant 固定时间点
     * @param zone 时区
     * @return 固定时钟提供器
     */
    static ClockProvider fixed(final Instant instant, final ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return () -> Clock.fixed(instant, zone);
    }
}
