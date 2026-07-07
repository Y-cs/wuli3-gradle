package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Internal fan-out for business alert notifiers.
 */
@Slf4j
public final class ErrorAlertNotifiers {

    private final List<ErrorAlertNotifier> notifiers;

    public ErrorAlertNotifiers(final List<ErrorAlertNotifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    public void dispatch(final ErrorAlertContext context) {
        for (final ErrorAlertNotifier notifier : this.notifiers) {
            try {
                notifier.alert(context);
            } catch (RuntimeException ex) {
                log.warn("Error alert notifier failed", ex);
            }
        }
    }
}
