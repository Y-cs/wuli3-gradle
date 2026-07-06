package com.kjs.wuli3.web.error;

import com.kjs.wuli3.core.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Resolves the HTTP status that should represent a handled web error.
 */
@FunctionalInterface
public interface WebErrorStatusResolver {

    /**
     * Return the HTTP status for the current error and external response code.
     */
    HttpStatus resolve(Throwable error, ErrorCode responseCode);
}
