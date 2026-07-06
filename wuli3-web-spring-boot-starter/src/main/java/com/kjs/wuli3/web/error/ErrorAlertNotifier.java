package com.kjs.wuli3.web.error;

/**
 * Extension point for business applications to alert handled web errors.
 */
@FunctionalInterface
public interface ErrorAlertNotifier {

    /**
     * Called after the unified exception handler captures an error.
     */
    void alert(final ErrorAlertContext context);
}
