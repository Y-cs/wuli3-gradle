package com.kjs.wuli3.json.datatype.resource;

/**
 * 针对指定资源类型转换存储路径与 JSON 边界值。
 *
 * @author GuoYang create on 2026/8/17 11:53
 */
public interface ResourcePathResolver {

    /**
     * 判断当前解析器是否支持指定资源类型。
     */
    boolean supports(String type);

    /**
     * 将存储的资源路径转换为 JSON 边界值。
     */
    String serialize(String type, String path);

    /**
     * 将 JSON 边界值还原为存储的资源路径。
     */
    String deserialize(String type, String url);
}
