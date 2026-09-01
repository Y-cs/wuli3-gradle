package com.kjs.wuli3.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.audit.payload.AuditLogOrigin;
import com.kjs.wuli3.audit.payload.AuditLogPayload;
import com.kjs.wuli3.audit.payload.AuditPrincipal;
import com.kjs.wuli3.propagation.context.PrincipalType;
import org.junit.jupiter.api.Test;

class AuditLogModelTest {

    @Test
    void entryRequiresStableBusinessFields() {
        assertThatThrownBy(() -> AuditLogEntry.success(" ", "order-1", "CREATE", "created"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("module cannot be blank");
        assertThatThrownBy(() -> AuditLogEntry.success("ORDER", " ", "CREATE", "created"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("targetId cannot be blank");
        assertThat(AuditLogEntry.failure("ORDER", "order-1", "CREATE", "failed").outcome())
                .isEqualTo(AuditLogOutcome.FAILURE);
    }

    @Test
    void originAllowsMissingCallContextButRejectsBlankValues() {
        final AuditLogOrigin origin = AuditLogOrigin.ofApplication("orders");

        assertThat(origin.application()).isEqualTo("orders");
        assertThat(origin.operator()).isNull();
        assertThat(origin.traceId()).isNull();
        assertThatThrownBy(() -> new AuditLogOrigin("orders", null, " ", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("requestId cannot be blank");
    }

    @Test
    void payloadKeepsBusinessContentSeparateFromOrigin() {
        final AuditLogEntry entry = AuditLogEntry.success("ORDER", "order-1", "CREATE", "created");
        final AuditLogOrigin origin = new AuditLogOrigin(
                "orders",
                new AuditPrincipal(PrincipalType.ADMIN, "42", "alice"),
                "request-1",
                "trace-1",
                "span-1",
                "203.0.113.8");
        final AuditLogPayload payload = new AuditLogPayload(AuditLogProtocol.SCHEMA_VERSION, entry, origin);

        assertThat(payload.entry()).isEqualTo(entry);
        assertThat(payload.origin().operator()).isEqualTo(new AuditPrincipal(PrincipalType.ADMIN, "42", "alice"));
        assertThatThrownBy(() -> new AuditLogPayload(0, entry, origin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schemaVersion must be greater than zero");
    }
}
