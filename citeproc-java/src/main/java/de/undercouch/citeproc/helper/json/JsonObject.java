package de.undercouch.citeproc.helper.json;

/**
 * Classes that implement this interface are able to convert their
 * contents to a JSON object
 * @author Michel Kraemer
 */
public interface JsonObject {
    /**
     * Converts this object to a JSON object
     * @param builder a builder that can be used to perform the conversion
     * @return the JSON object
     */
    Object toJson(JsonBuilder builder);
}
