package de.undercouch.citeproc.helper.json;

/**
 * Builds JSON objects
 * @author Michel Kraemer
 */
public interface JsonBuilder {
    /**
     * Adds a property to the object to build
     * @param name the property's name
     * @param o the property's value
     * @return the {@link JsonBuilder}
     */
    JsonBuilder add(String name, Object o);

    /**
     * Builds the JSON object
     * @return the object
     */
    Object build();

    /**
     * Converts the given object to a JSON object
     * @param o the object to convert
     * @return the JSON object
     */
    Object toJson(Object o);
}
