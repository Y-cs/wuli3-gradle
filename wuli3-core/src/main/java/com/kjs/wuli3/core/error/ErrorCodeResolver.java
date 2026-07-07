package com.kjs.wuli3.core.error;

/**
 * Converts an {@link ErrorCode} into the external code string exposed by adapters such as web responses.
 */
public interface ErrorCodeResolver {

    String resolver(final ErrorCode errorCode);
}
