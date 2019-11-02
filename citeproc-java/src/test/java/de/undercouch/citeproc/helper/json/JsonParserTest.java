package de.undercouch.citeproc.helper.json;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link JsonParser}
 * @author Michel Kraemer
 */
public class JsonParserTest {
    /**
     * Tests if a simple object consisting of several name-value pairs
     * can be read
     * @throws IOException if the test failed
     */
    @Test
    public void simpleObject() throws IOException {
        String obj = "{\"name\":\"value\",\"int\":1302,\"float\":1.57,"
                + "\"negint\":-5,\"negfloat\":-1.57,\"floatexp\":-1.5e7}";
        JsonLexer l = new JsonLexer(new StringReader(obj));
        JsonParser p = new JsonParser(l);
        Map<String, Object> m = p.parseObject();

        assertEquals(6, m.size());
        assertEquals("value", m.get("name"));
        assertEquals(1302L, m.get("int"));
        assertEquals(1.57, m.get("float"));
        assertEquals(-5L, m.get("negint"));
        assertEquals(-1.57, m.get("negfloat"));
        assertEquals(-1.5e7, m.get("floatexp"));
    }

    /**
     * Tests if embedded objects and embedded arrays can be read
     * @throws IOException if the test failed
     */
    @Test
    public void embedded() throws IOException {
        String obj = "{\"authors\":[\"Ted\", \"Mark\"],\"date\": {\"year\":2013,\"month\":9}}";
        JsonLexer l = new JsonLexer(new StringReader(obj));
        JsonParser p = new JsonParser(l);
        Map<String, Object> m = p.parseObject();
        assertEquals(2, m.size());

        @SuppressWarnings("unchecked")
        List<String> authors = (List<String>)m.get("authors");
        assertEquals(2, authors.size());
        assertEquals("Ted", authors.get(0));
        assertEquals("Mark", authors.get(1));

        @SuppressWarnings("unchecked")
        Map<String, Object> date = (Map<String, Object>)m.get("date");
        assertEquals(2, date.size());
        assertEquals(2013L, date.get("year"));
        assertEquals(9L, date.get("month"));
    }
}
