package com.kjs.wuli3.aliyun.error;

import com.kjs.wuli3.core.error.ErrorCodeException;
import java.io.Serial;

/**
 * OSS 操作失败异常。
 * @author GuoYang create on 2026/8/12 17:37
 */
public class OssOperateException extends ErrorCodeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public OssOperateException(String message) {
        super(OssErrorCode.OPERATE_ERROR, message);
    }

    public OssOperateException(String message, Throwable cause) {
        super(OssErrorCode.OPERATE_ERROR, message, cause);
    }

    public OssOperateException(Throwable cause) {
        super(OssErrorCode.OPERATE_ERROR, cause);
    }
}
