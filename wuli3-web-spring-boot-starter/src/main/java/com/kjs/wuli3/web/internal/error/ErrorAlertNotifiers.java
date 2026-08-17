package com.kjs.wuli3.web.internal.error;

import com.kjs.wuli3.web.error.ErrorAlertContext;
import com.kjs.wuli3.web.error.ErrorAlertNotifier;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 向业务告警通知器分发事件的内部组件。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@Slf4j
public final class ErrorAlertNotifiers {

    private final List<ErrorAlertNotifier> notifiers;

    public ErrorAlertNotifiers(final List<ErrorAlertNotifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    /** 向所有通知器分发错误告警；单个通知器失败不会阻断其他通知器。 */
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
