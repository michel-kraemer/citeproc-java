package de.undercouch.citeproc.helper.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import static de.undercouch.citeproc.helper.StringHelper.escapeJava;

/**
 * A JSON builder that creates JSON strings
 * @author Michel Kraemer
 */
public class StringJsonBuilder implements JsonBuilder {
    private final JsonBuilderFactory factory;
    private final StringBuilder b;
    private int c = 0;

    /**
     * Creates a JSON builder
     * @param factory the factory that created this builder
     */
    public StringJsonBuilder(JsonBuilderFactory factory) {
        this.factory = factory;
        b = new StringBuilder("{");
    }

    @Override
    public JsonBuilder add(String name, Object o) {
        if (c > 0) {
            b.append(",");
        }
        ++c;

        b.append("\"").append(name).append("\":");
        b.append(toJson(o, factory).toString());

        return this;
    }

    @Override
    public String build() {
        b.append("}");
        return b.toString();
    }

    @Override
    public Object toJson(Object arr) {
        return toJson(arr, factory);
    }

    /**
     * Converts an object to a JSON object. The given object can be a
     * {@link JsonObject}, a primitive, a string, or an array. Converts
     * the object to a string via {@link Object#toString()} if its type
     * is unknown and then converts this string to JSON.
     * @param obj the object to convert
     * @param factory a factory used to create JSON builders
     * @return the JSON object
     */
    private static Object toJson(Object obj, JsonBuilderFactory factory) {
        if (obj instanceof JsonObject) {
            return toJson((JsonObject)obj, factory);
        } else if (obj instanceof Number) {
            return toJson((Number)obj);
        } else if (obj instanceof Boolean) {
            return toJson(((Boolean)obj).booleanValue());
        } else if (obj.getClass().isArray()) {
            StringBuilder r = new StringBuilder();
            int len = Array.getLength(obj);
            for (int i = 0; i < len; ++i) {
                Object ao = Array.get(obj, i);
                if (r.length() > 0) {
                    r.append(",");
                }
                r.append(toJson(ao, factory));
            }
            return "[" + r + "]";
        } else if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>)obj;
            StringBuilder r = new StringBuilder();
            for (Object ao : coll) {
                if (r.length() > 0) {
                    r.append(",");
                }
                r.append(toJson(ao, factory));
            }
            return "[" + r + "]";
        } else if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>)obj;
            StringBuilder r = new StringBuilder();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (r.length() > 0) {
                    r.append(",");
                }
                r.append(toJson(e.getKey(), factory));
                r.append(":");
                r.append(toJson(e.getValue(), factory));
            }
            return "{" + r + "}";
        }
        return toJson(String.valueOf(obj));
    }

    /**
     * Converts a string to a JSON string. Escapes special characters
     * if necessary.
     * @param s the string to convert
     * @return the JSON string
     */
    private static String toJson(String s) {
        return "\"" + escapeJava(s) + "\"";
    }

    /**
     * Converts a boolean to a JSON string
     * @param b the boolean to convert
     * @return the JSON string
     */
    private static String toJson(boolean b) {
        return String.valueOf(b);
    }

    /**
     * Converts a number to a JSON string
     * @param n the number to convert
     * @return the JSON string
     */
    private static String toJson(Number n) {
        return String.valueOf(n);
    }

    /**
     * Converts a {@link JsonObject} to a JSON string
     * @param obj the object to convert
     * @param factory a factory used to create JSON builders
     * @return the JSON string
     */
    private static Object toJson(JsonObject obj, JsonBuilderFactory factory) {
        return obj.toJson(factory.createJsonBuilder());
    }
}
