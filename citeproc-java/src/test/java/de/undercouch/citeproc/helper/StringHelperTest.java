package de.undercouch.citeproc.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test {@link StringHelper}
 * @author Michel Kraemer
 */
public class StringHelperTest {
    /**
     * Sanitize some simple strings
     */
    @Test
    public void sanitize() {
        assertEquals("Kramer", StringHelper.sanitize("Kr\u00E4mer"));
        assertEquals("Giessen", StringHelper.sanitize("Gie\u00dfen"));
        assertEquals("Elsyee", StringHelper.sanitize("Elsy\u00e9e"));
        assertEquals("Champs_Elysees", StringHelper.sanitize("Champs-\u00c9lys\u00e9es"));
        assertEquals("A_test_with_spaces", StringHelper.sanitize("A test with spaces"));
        assertEquals("any_thing_else", StringHelper.sanitize("any+thing*else"));
        assertEquals("Numbers_0124", StringHelper.sanitize("Numbers 0124"));
    }

    /**
     * Tests {@link StringHelper#escapeJava(String)}
     */
    @Test
    public void escapeJava() {
        assertNull(StringHelper.escapeJava(null));
        assertEscapeJava("", "");
        assertEscapeJava("test", "test");
        assertEscapeJava("\\t", "\t");
        assertEscapeJava("\\\\", "\\");
        assertEscapeJava("'", "'");
        assertEscapeJava("\\\"", "\"");
        assertEscapeJava("/", "/");
        assertEscapeJava("\\\\\\b\\r", "\\\b\r");
        assertEscapeJava("\\u4711", "\u4711");
        assertEscapeJava("\\u0815", "\u0815");
        assertEscapeJava("\\u0080", "\u0080");
        assertEscapeJava("\u007f", "\u007f");
        assertEscapeJava(" ", "\u0020");
        assertEscapeJava("\\u0000", "\u0000");
        assertEscapeJava("\\u001F", "\u001f");
    }

    private void assertEscapeJava(String expected, String original) {
        String r = StringHelper.escapeJava(original);
        assertEquals(expected, r);
    }
}
