package de.undercouch.citeproc.helper.json;

/**
 * A {@link JsonBuilderFactory} that always builds {@link StringJsonBuilder}s
 * @author Michel Kraemer
 */
public class StringJsonBuilderFactory implements JsonBuilderFactory {
    @Override
    public JsonBuilder createJsonBuilder() {
        return new StringJsonBuilder(this);
    }
}
