package com.kjs.wuli3.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuditLogContractsTest {

    @Test
    void commandRequiresStableBusinessFields() {
        assertThatThrownBy(() -> AuditLogCommand.success(" ", "order-1", "CREATE", "created"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("module cannot be blank");
        assertThatThrownBy(() -> AuditLogCommand.success("ORDER", " ", "CREATE", "created"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("targetId cannot be blank");
    }

    @Test
    void queryUsesFrameworkIndependentPagination() {
        final AuditLogQuery query = AuditLogQuery.all(2, 50);
        final AuditLogPage page = new AuditLogPage(List.of(), 0, query.pageNumber(), query.pageSize());

        assertThat(page.pageNumber()).isEqualTo(2);
        assertThat(page.pageSize()).isEqualTo(50);
        assertThatThrownBy(() -> AuditLogQuery.all(0, 201))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pageSize must be between 1 and 200");
    }

    @Test
    void viewKeepsDatabaseAndEventIdentifiersSeparate() {
        final AuditLogPayload payload = new AuditLogPayload(
                1,
                "orders",
                "ORDER",
                "order-1",
                "CREATE",
                "created",
                AuditLogOutcome.SUCCESS,
                null,
                null,
                null,
                null,
                null);
        final AuditLogView view = new AuditLogView(7L, "event-1", Instant.EPOCH, Instant.EPOCH, payload);

        assertThat(view.logId()).isEqualTo(7L);
        assertThat(view.eventId()).isEqualTo("event-1");
    }
}
