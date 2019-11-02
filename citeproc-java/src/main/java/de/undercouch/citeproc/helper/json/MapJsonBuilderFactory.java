package de.undercouch.citeproc.helper.json;

/**
 * A {@link JsonBuilderFactory} that always builds {@link MapJsonBuilder}s
 * @author Michel Kraemer
 */
public class MapJsonBuilderFactory implements JsonBuilderFactory {
    @Override
    public JsonBuilder createJsonBuilder() {
        return new MapJsonBuilder(this);
    }
}
