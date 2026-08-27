plugins {
    id("com.kjs.wuli3.spring-conventions")
}

description = "OpenTelemetry Java Agent integration, trace context access, and business metrics."

dependencies {
    api("io.opentelemetry:opentelemetry-api")
}
