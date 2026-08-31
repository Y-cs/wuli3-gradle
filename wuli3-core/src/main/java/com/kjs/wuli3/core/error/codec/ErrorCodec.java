package com.kjs.wuli3.core.error.codec;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.core.error.ErrorPropagationProtocol;
import com.kjs.wuli3.core.error.ErrorVisibility;

/**
 * 在服务边界统一序列化和反序列化错误。
 *
 * @author GuoYang create on 2026/8/28 20:00
 */
public interface ErrorCodec {

    /** 将本地错误转换为不依赖业务枚举的传输值。 */
    ErrorPropagationProtocol serialize(ErrorCodeException exception, ErrorVisibility visibility);

    /** 将传输值还原为本地统一异常。 */
    ErrorCodeException deserialize(ErrorPropagationProtocol protocol);
}
