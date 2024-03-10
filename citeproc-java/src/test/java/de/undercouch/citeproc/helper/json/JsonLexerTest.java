package de.undercouch.citeproc.helper.json;

import de.undercouch.citeproc.helper.json.JsonLexer.Type;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link JsonLexer}
 * @author Michel Kraemer
 */
public class JsonLexerTest {
    /**
     * Tests if a simple object consisting of several name-value pairs
     * can be read
     * @throws IOException if the test failed
     */
    @Test
    public void simpleObject() throws IOException {
        String obj = "{\"name\":\"value\",\"int\":1302,\"float\":1.57,"
                + "\"negint\":-5,\"negfloat\":-1.57,\"floatexp\":-1.5e7}";
        JsonLexer p = new JsonLexer(new StringReader(obj));

        Type t = p.readNextToken();
        assertEquals(Type.START_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String name = p.readString();
        assertEquals("name", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String value = p.readString();
        assertEquals("value", value);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        name = p.readString();
        assertEquals("int", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        int i = p.readNumber().intValue();
        assertEquals(1302, i);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        name = p.readString();
        assertEquals("float", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        float f = p.readNumber().floatValue();
        assertEquals(1.57f, f, 0.0);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        name = p.readString();
        assertEquals("negint", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        i = p.readNumber().intValue();
        assertEquals(-5, i);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        name = p.readString();
        assertEquals("negfloat", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        f = p.readNumber().floatValue();
        assertEquals(-1.57f, f, 0.0);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        name = p.readString();
        assertEquals("floatexp", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        f = p.readNumber().floatValue();
        assertEquals(-1.5e7, f, 0.0);

        t = p.readNextToken();
        assertEquals(Type.END_OBJECT, t);
    }

    /**
     * Tests if a unicode string can be read
     * @throws IOException if the test failed
     */
    @Test
    public void unicode() throws IOException {
        String obj = "{\"name\":\"Michel Kr\\u00E4mer\"}";
        JsonLexer p = new JsonLexer(new StringReader(obj));

        Type t = p.readNextToken();
        assertEquals(Type.START_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String name = p.readString();
        assertEquals("name", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String value = p.readString();
        assertEquals("Michel Krämer", value);

        t = p.readNextToken();
        assertEquals(Type.END_OBJECT, t);
    }

    /**
     * Tests if embedded objects and embedded arrays can be read
     * @throws IOException if the test failed
     */
    @Test
    public void embedded() throws IOException {
        String obj = "{\"authors\":[\"Ted\", \"Mark\"],\"date\": {\"year\":2013,\"month\":9}}";
        JsonLexer p = new JsonLexer(new StringReader(obj));

        Type t = p.readNextToken();
        assertEquals(Type.START_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String name = p.readString();
        assertEquals("authors", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.START_ARRAY, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String value = p.readString();
        assertEquals("Ted", value);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        value = p.readString();
        assertEquals("Mark", value);

        t = p.readNextToken();
        assertEquals(Type.END_ARRAY, t);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        value = p.readString();
        assertEquals("date", value);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.START_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        value = p.readString();
        assertEquals("year", value);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        int i = p.readNumber().intValue();
        assertEquals(2013, i);

        t = p.readNextToken();
        assertEquals(Type.COMMA, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        value = p.readString();
        assertEquals("month", value);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.NUMBER, t);
        i = p.readNumber().intValue();
        assertEquals(9, i);

        t = p.readNextToken();
        assertEquals(Type.END_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.END_OBJECT, t);
    }

    /**
     * Tests if a string with a slash can be read
     * @throws IOException if the test failed
     */
    @Test
    public void slash() throws IOException {
        String obj = "{\"name\":\"Michel\\/Kraemer\"}";
        JsonLexer p = new JsonLexer(new StringReader(obj));

        Type t = p.readNextToken();
        assertEquals(Type.START_OBJECT, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String name = p.readString();
        assertEquals("name", name);

        t = p.readNextToken();
        assertEquals(Type.COLON, t);

        t = p.readNextToken();
        assertEquals(Type.STRING, t);
        String value = p.readString();
        assertEquals("Michel/Kraemer", value);

        t = p.readNextToken();
        assertEquals(Type.END_OBJECT, t);
    }
}
