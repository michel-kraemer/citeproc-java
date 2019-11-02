package de.undercouch.citeproc.helper.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the JSON builder that creates JSON strings
 * @author Michel Kraemer
 */
public class StringJsonBuilderTest {
    private JsonBuilderFactory factory = new StringJsonBuilderFactory();

    /**
     * Tests if a string array is converted correctly
     */
    @Test
    public void toJsonStringArray() {
        String[] a = new String[] {
                "a", "b", "c", "That's it"
        };
        assertEquals("[\"a\",\"b\",\"c\",\"That's it\"]",
                factory.createJsonBuilder().toJson(a));
    }

    /**
     * Tests if a JsonObject is converted correctly
     */
    @Test
    public void toJsonObject() {
        JsonObject obj = builder -> {
            int[][] g = new int[][] { new int[] { 1, 2 }, new int[] { 3, 4 } };
            builder.add("a", "test");
            builder.add("b", "that's it");
            builder.add("c", "var s = \"Hello\"");
            builder.add("d", true);
            builder.add("e", false);
            builder.add("f", 42);
            builder.add("g", g);
            return builder.build();
        };
        assertEquals("{\"a\":\"test\",\"b\":\"that's it\",\"c\":\"var s = "
                + "\\\"Hello\\\"\",\"d\":true,\"e\":false,\"f\":42,\"g\":"
                + "[[1,2],[3,4]]}", obj.toJson(factory.createJsonBuilder()));
    }
}
