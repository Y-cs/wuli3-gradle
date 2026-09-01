package com.kjs.wuli3.web.internal.context;

import com.kjs.wuli3.web.context.ClientIpResolver;
import com.kjs.wuli3.web.context.WebContextProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * 仅信任来自已配置代理网段转发头的默认客户端 IP 解析器。
 *
 * <p><strong>安全策略：可信代理网段验证</strong></p>
 * <p>本实现通过 CIDR 白名单验证直接连接的 peer 地址，只有当 {@code request.getRemoteAddr()} 命中
 * {@code trusted-proxy-cidrs} 配置的网段时，才会读取转发头（如 {@code X-Forwarded-For}、{@code X-Real-IP}）。
 * 这防止了恶意客户端通过伪造转发头绕过 IP 限制或污染日志。
 *
 * <h2>转发头链验证</h2>
 * <p>对于 {@code X-Forwarded-For} 等链式转发头，本实现会从右向左遍历 IP 链，直到找到第一个不在可信网段内的 IP：
 * <ul>
 *   <li>遍历从最右侧（最接近当前服务的代理）开始</li>
 *   <li>跳过所有可信代理 IP</li>
 *   <li>返回第一个不可信 IP（即真实客户端或第一个外部代理）</li>
 *   <li>如果整个链都是可信代理，返回链头（最左侧）IP</li>
 * </ul>
 *
 * <h2>标准 Forwarded 头支持</h2>
 * <p>支持 RFC 7239 {@code Forwarded} 头格式：{@code for=<ip>;proto=<protocol>}，会解析 {@code for} 参数并应用相同的链验证逻辑。
 *
 * <h2>配置示例</h2>
 * <pre>
 * wuli3.web.context.trusted-proxy-cidrs=10.0.0.0/8,192.168.0.0/16
 * wuli3.web.context.client-ip-header-priority=X-Forwarded-For,X-Real-IP,Forwarded
 * </pre>
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public final class DefaultClientIpResolver implements ClientIpResolver {

    private static final String FORWARDED = "Forwarded";
    private static final String FORWARDED_FOR = "for";

    private final WebContextProperties properties;
    private final List<IpNetwork> trustedProxyNetworks;

    public DefaultClientIpResolver(final WebContextProperties properties) {
        this.properties = properties;
        this.trustedProxyNetworks =
                properties.getTrustedProxyCidrs().stream().map(IpNetwork::parse).toList();
    }

    @Override
    public String resolve(final HttpServletRequest request) {
        final String remoteAddr = request.getRemoteAddr();
        if (this.isTrustedPeer(remoteAddr)) {
            for (final String headerName : this.properties.getClientIpHeaderPriority()) {
                final String candidate = this.candidate(request, headerName, remoteAddr);
                if (candidate != null && !candidate.isBlank()) {
                    return candidate;
                }
            }
        }
        return remoteAddr;
    }

    private boolean isTrustedPeer(final String remoteAddr) {
        if (this.trustedProxyNetworks.isEmpty()) {
            return false;
        }
        final byte @Nullable [] address = DefaultClientIpResolver.parseAddress(remoteAddr);
        if (address == null) {
            return false;
        }
        return this.isTrustedAddress(address);
    }

    private boolean isTrustedAddress(final byte[] address) {
        return this.trustedProxyNetworks.stream().anyMatch(network -> network.contains(address));
    }

    private @Nullable String candidate(
            final HttpServletRequest request, final String headerName, final String remoteAddr) {
        final String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return null;
        }
        if (FORWARDED.equalsIgnoreCase(headerName)) {
            return this.forwardedFor(value, remoteAddr);
        }
        return this.forwardedForChain(value, remoteAddr);
    }

    private @Nullable String forwardedFor(final String value, final String remoteAddr) {
        final StringBuilder chain = new StringBuilder();
        int start = 0;
        while (start <= value.length()) {
            final int comma = value.indexOf(',', start);
            final String entry = comma < 0 ? value.substring(start) : value.substring(start, comma);
            final String forwardedFor = DefaultClientIpResolver.forwardedEntryFor(entry);
            if (forwardedFor != null) {
                if (!chain.isEmpty()) {
                    chain.append(',');
                }
                chain.append(forwardedFor);
            }
            if (comma < 0) {
                break;
            }
            start = comma + 1;
        }
        return chain.isEmpty() ? null : this.forwardedForChain(chain.toString(), remoteAddr);
    }

    private static @Nullable String forwardedEntryFor(final String entry) {
        int start = 0;
        while (start <= entry.length()) {
            final int semicolon = entry.indexOf(';', start);
            final String part = semicolon < 0 ? entry.substring(start) : entry.substring(start, semicolon);
            final String trimmed = part.trim();
            final int equals = trimmed.indexOf('=');
            if (equals > 0
                    && DefaultClientIpResolver.FORWARDED_FOR.equalsIgnoreCase(
                            trimmed.substring(0, equals).trim())) {
                return DefaultClientIpResolver.unquote(
                        trimmed.substring(equals + 1).trim());
            }
            if (semicolon < 0) {
                break;
            }
            start = semicolon + 1;
        }
        return null;
    }

    private @Nullable String forwardedForChain(final String value, final String remoteAddr) {
        final List<String> hops = DefaultClientIpResolver.forwardedHops(value);
        if (hops.isEmpty()) {
            return null;
        }
        final byte @Nullable [] remoteAddress = DefaultClientIpResolver.parseAddress(remoteAddr);
        if (remoteAddress == null || !this.isTrustedAddress(remoteAddress)) {
            return null;
        }

        int index = hops.size() - 1;
        while (index >= 0) {
            final byte @Nullable [] hopAddress = DefaultClientIpResolver.parseAddress(hops.get(index));
            if (hopAddress == null) {
                return null;
            }
            if (!this.isTrustedAddress(hopAddress)) {
                return DefaultClientIpResolver.normalizeAddress(hops.get(index));
            }
            index--;
        }
        return DefaultClientIpResolver.normalizeAddress(hops.get(0));
    }

    private static List<String> forwardedHops(final String value) {
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .toList();
    }

    private static String unquote(final String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String normalizeAddress(final String value) {
        final String trimmed = DefaultClientIpResolver.unquote(value.trim());
        if (trimmed.startsWith("[") && trimmed.contains("]")) {
            return trimmed.substring(1, trimmed.indexOf(']'));
        }
        final int colon = trimmed.indexOf(':');
        if (colon > 0
                && trimmed.indexOf(':', colon + 1) < 0
                && trimmed.substring(colon + 1).chars().allMatch(Character::isDigit)) {
            return trimmed.substring(0, colon);
        }
        return trimmed;
    }

    private static byte @Nullable [] parseAddress(final String value) {
        final String normalized = DefaultClientIpResolver.normalizeAddress(value);
        if (!DefaultClientIpResolver.isNumericAddress(normalized)) {
            return null;
        }
        try {
            return InetAddress.getByName(normalized).getAddress();
        } catch (final UnknownHostException ex) {
            return null;
        }
    }

    private static boolean isNumericAddress(final String value) {
        final String lower = value.toLowerCase(Locale.ROOT);
        return lower.chars()
                .allMatch(ch -> (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || ch == '.' || ch == ':');
    }

    private static final class IpNetwork {

        private final byte[] address;
        private final int prefixLength;

        private IpNetwork(final byte[] address, final int prefixLength) {
            this.address = address.clone();
            this.prefixLength = prefixLength;
        }

        private static IpNetwork parse(final String cidr) {
            final String trimmed = cidr.trim();
            final int slash = trimmed.indexOf('/');
            final String addressText = slash < 0 ? trimmed : trimmed.substring(0, slash);
            final byte @Nullable [] parsedAddress = DefaultClientIpResolver.parseAddress(addressText);
            if (parsedAddress == null) {
                throw new IllegalArgumentException("Invalid trusted proxy CIDR: " + cidr);
            }
            final int maxPrefix = parsedAddress.length * Byte.SIZE;
            final int parsedPrefix = slash < 0
                    ? maxPrefix
                    : DefaultClientIpResolver.parsePrefix(trimmed.substring(slash + 1), maxPrefix, cidr);
            return new IpNetwork(parsedAddress, parsedPrefix);
        }

        private boolean contains(final byte[] candidate) {
            if (candidate.length != this.address.length) {
                return false;
            }
            int remainingBits = this.prefixLength;
            for (int index = 0; index < this.address.length && remainingBits > 0; index++) {
                final int bits = Math.min(Byte.SIZE, remainingBits);
                final int mask = 0xFF << (Byte.SIZE - bits);
                if ((this.address[index] & mask) != (candidate[index] & mask)) {
                    return false;
                }
                remainingBits -= bits;
            }
            return true;
        }
    }

    private static int parsePrefix(final String value, final int maxPrefix, final String cidr) {
        try {
            final int prefix = Integer.parseInt(value);
            if (prefix >= 0 && prefix <= maxPrefix) {
                return prefix;
            }
        } catch (final NumberFormatException ignored) {
            // handled below with a message that includes the full CIDR value
        }
        throw new IllegalArgumentException("Invalid trusted proxy CIDR: " + cidr);
    }
}
