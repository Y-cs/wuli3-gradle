package com.kjs.wuli3.json.datatype.desensitization;

import cn.hutool.core.util.DesensitizedUtil;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 按稳定语义键解析脱敏策略。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DesensitizationStrategyRegistry {
    private final Map<String, DesensitizationStrategy> strategies;

    private DesensitizationStrategyRegistry(final Map<String, DesensitizationStrategy> strategies) {
        this.strategies = Map.copyOf(strategies);
    }

    public static DesensitizationStrategyRegistry standard() {
        return DesensitizationStrategyRegistry.standardWithOverrides(List.of());
    }

    public static DesensitizationStrategyRegistry standardWithOverrides(
            final Collection<? extends DesensitizationStrategy> overrides) {
        final Map<String, DesensitizationStrategy> strategies = new LinkedHashMap<>();
        DesensitizationStrategyRegistry.add(
                strategies,
                DesensitizationStrategy.of(
                        DesensitizationTypes.PHONE,
                        value -> value.length() <= 7
                                ? DesensitizedUtil.password(value)
                                : DesensitizedUtil.mobilePhone(value)));
        DesensitizationStrategyRegistry.add(
                strategies,
                DesensitizationStrategy.of(
                        DesensitizationTypes.EMAIL, DesensitizationStrategyRegistry::desensitizeEmail));
        DesensitizationStrategyRegistry.add(
                strategies,
                DesensitizationStrategy.of(
                        DesensitizationTypes.ID_CARD,
                        value -> value.length() <= 10
                                ? DesensitizedUtil.password(value)
                                : DesensitizedUtil.idCardNum(value, 6, 4)));
        DesensitizationStrategyRegistry.add(
                strategies,
                DesensitizationStrategy.of(
                        DesensitizationTypes.BANK_CARD,
                        value -> value.length() < 9
                                ? DesensitizedUtil.password(value)
                                : DesensitizedUtil.bankCard(value)));
        for (final DesensitizationStrategy override : overrides) {
            DesensitizationStrategyRegistry.add(strategies, override);
        }
        return new DesensitizationStrategyRegistry(strategies);
    }

    public Optional<DesensitizationStrategy> find(final String type) {
        return Optional.ofNullable(this.strategies.get(type));
    }

    private static String desensitizeEmail(final String value) {
        final int atIndex = value.indexOf('@');
        if (atIndex <= 1 || atIndex == value.length() - 1) {
            return DesensitizedUtil.password(value);
        }
        return DesensitizedUtil.email(value);
    }

    private static void add(
            final Map<String, DesensitizationStrategy> strategies, final DesensitizationStrategy strategy) {
        final DesensitizationStrategy requiredStrategy = Objects.requireNonNull(strategy, "strategy");
        strategies.put(requiredStrategy.type(), requiredStrategy);
    }
}
