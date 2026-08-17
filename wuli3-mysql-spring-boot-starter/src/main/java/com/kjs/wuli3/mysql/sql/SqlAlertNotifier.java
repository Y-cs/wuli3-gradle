package com.kjs.wuli3.mysql.sql;

/**
 * 慢 SQL 告警扩展点，业务应用可接入自己的告警平台。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
@FunctionalInterface
public interface SqlAlertNotifier {

    /** 接收一条慢 SQL 事件。 */
    void alert(SqlAlertContext context);
}
