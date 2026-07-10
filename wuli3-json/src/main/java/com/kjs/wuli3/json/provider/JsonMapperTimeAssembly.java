package com.kjs.wuli3.json.provider;

import com.fasterxml.jackson.databind.json.JsonMapper;
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
 * Applies project date/time formats and default locale/time zone.
 */
public final class JsonMapperTimeAssembly implements JsonMapperAssemblyChain {
    @Override
    public void assemble(final JsonMapper.Builder mapperBuilder) {
        mapperBuilder.addModule(this.javaTimeModule())
                .defaultLocale(this.defaultLocale())
                .defaultTimeZone(this.defaultTimeZone());
    }

    public JavaTimeModule javaTimeModule() {
        final JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormats.DATE));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormats.DATE));
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormats.TIME));
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormats.TIME));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormats.DATE_TIME));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormats.DATE_TIME));
        return module;
    }

    public Locale defaultLocale() {
        return Locale.ROOT;
    }

    public TimeZone defaultTimeZone() {
        return TimeZone.getTimeZone(DateTimeFormats.DEFAULT_ZONE_ID);
    }
}
