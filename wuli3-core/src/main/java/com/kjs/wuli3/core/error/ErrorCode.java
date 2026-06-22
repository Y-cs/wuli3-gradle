package com.kjs.wuli3.core.error;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    String code();

    String message();
}
