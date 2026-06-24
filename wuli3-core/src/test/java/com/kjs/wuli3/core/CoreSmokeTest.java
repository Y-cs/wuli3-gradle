package com.kjs.wuli3.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kjs.wuli3.core.exception.CommonErrorCode;
import com.kjs.wuli3.core.error.SystemException;
import com.kjs.wuli3.core.function.Functions;
import com.kjs.wuli3.core.page.PageQuery;
import com.kjs.wuli3.core.page.PageResult;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreSmokeTest {
    @Test
    void pageQueryCalculatesOffset() {
        assertThat(new PageQuery(2, 20).offset()).isEqualTo(20);
    }

    @Test
    void pageResultCopiesRecords() {
        PageResult<String> result = new PageResult<>(List.of("a"), 1, 1, 10);
        assertThat(result.records()).containsExactly("a");
    }

    @Test
    void checkedExceptionIsWrapped() {
        assertThatThrownBy(() -> Functions.uncheckedSupplier(() -> {
            throw new IOException("io");
        }).get())
                .isInstanceOf(SystemException.class)
                .hasMessage("io");
    }

    @Test
    void commonErrorCodeExposesCode() {
        assertThat(CommonErrorCode.SUCCESS.code()).isEqualTo("0");
    }
}
