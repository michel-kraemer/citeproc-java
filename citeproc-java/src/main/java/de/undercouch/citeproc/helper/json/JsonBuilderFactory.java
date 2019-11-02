package de.undercouch.citeproc.helper.json;

/**
 * A factory for {@link JsonBuilder} objects
 * @author Michel Kraemer
 */
public interface JsonBuilderFactory {
    /**
     * @return a new JSON builder
     */
    JsonBuilder createJsonBuilder();
}
