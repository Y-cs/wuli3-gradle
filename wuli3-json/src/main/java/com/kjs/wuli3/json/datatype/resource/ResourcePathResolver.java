package com.kjs.wuli3.json.datatype.resource;

/**
 * Converts resource paths and JSON-facing resource values for a specific resource type.
 */
public interface ResourcePathResolver {

    /**
     * Returns whether this resolver can handle the supplied resource type.
     */
    boolean supports(String type);

    /**
     * Converts a stored resource path to the JSON-facing value.
     */
    String serialize(String type, String path);

    /**
     * Restores a JSON-facing value to the stored resource path.
     */
    String deserialize(String type, String url);
}
