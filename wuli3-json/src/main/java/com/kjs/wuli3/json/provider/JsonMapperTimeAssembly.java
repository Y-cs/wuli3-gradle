package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.kjs.wuli3.core.time.DateTimeFormats;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 应用项目日期时间格式、默认区域设置和时区。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class JsonMapperTimeAssembly implements JsonMapperAssemblyChain {
    /** 应用 Java 时间模块、默认区域设置和默认时区。 */
    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
        mapperBuilder
                .addModule(this.javaTimeModule())
                .defaultLocale(this.defaultLocale())
                .defaultTimeZone(this.defaultTimeZone());
    }

    /** 创建使用项目日期时间格式的 Java 时间模块。 */
    public JavaTimeModule javaTimeModule() {
        return this.configure(new JavaTimeModule());
    }

    /**
     * 创建具有唯一标识的时间模块，用于覆盖容器默认的 Java 时间模块。
     *
     * @return 独立持有且使用项目时间格式的模块
     */
    public SimpleModule javaTimeOverrideModule() {
        return this.configure(new SimpleModule("wuli3-java-time"));
    }

    private <M extends SimpleModule> M configure(final M module) {
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormats.DATE));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormats.DATE));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormats.TIME));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormats.TIME));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormats.DATE_TIME));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormats.DATE_TIME));
        return module;
    }

    /** 返回项目默认区域设置。 */
    public Locale defaultLocale() {
        return Locale.ROOT;
    }

    /** 返回项目默认时区。 */
    public TimeZone defaultTimeZone() {
        return TimeZone.getTimeZone(DateTimeFormats.DEFAULT_ZONE_ID);
    }
}
