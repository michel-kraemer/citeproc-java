package de.undercouch.citeproc.helper.json;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the JSON builder that creates maps
 * @author Michel Kraemer
 */
public class MapJsonBuilderTest {
    private JsonBuilderFactory factory = new MapJsonBuilderFactory();

    /**
     * Tests if a string array is converted correctly
     */
    @Test
    public void toJsonStringArray() {
        String[] a = new String[] {
                "a", "b", "c", "That's it"
        };
        List<String> l = Arrays.asList(a);
        assertEquals(l, factory.createJsonBuilder().toJson(a));
    }

    /**
     * Tests if a JsonObject is converted correctly
     */
    @Test
    @SuppressWarnings("unchecked")
    public void toJsonObject() {
        JsonObject obj = builder -> {
            int[][] g = new int[][] { new int[] { 1, 2 }, new int[] { 3, 4 } };
            builder.add("a", "test");
            builder.add("d", true);
            builder.add("f", 42);
            builder.add("g", g);
            return builder.build();
        };
        Map<String, Object> m = (Map<String, Object>)obj.toJson(factory.createJsonBuilder());
        assertEquals(4, m.size());
        assertEquals(true, m.get("d"));
        assertEquals(42, m.get("f"));
        assertEquals(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4)), m.get("g"));
    }
}
