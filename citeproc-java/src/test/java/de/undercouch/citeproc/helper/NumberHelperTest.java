package de.undercouch.citeproc.helper;

import de.undercouch.citeproc.helper.NumberHelper.NumberToken;
import de.undercouch.citeproc.helper.NumberHelper.NumberTokenType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link NumberHelper}
 * @author Michel Kraemer
 */
public class NumberHelperTest {
    /**
     * Tests the {@link NumberHelper#isNumeric} method
     */
    @Test
    public void isNumeric() {
        assertTrue(NumberHelper.isNumeric("1"));
        assertTrue(NumberHelper.isNumeric("935"));
        assertTrue(NumberHelper.isNumeric("a13"));
        assertTrue(NumberHelper.isNumeric("7B"));
        assertTrue(NumberHelper.isNumeric("as782u"));
        assertTrue(NumberHelper.isNumeric("1-2"));
        assertTrue(NumberHelper.isNumeric("42-0815,47&11"));
        assertTrue(NumberHelper.isNumeric("42 -   0815 , 47   & 11"));
        assertTrue(NumberHelper.isNumeric("42a -  tt0815 ,  ll47uu &o11"));

        assertFalse(NumberHelper.isNumeric("a"));
        assertFalse(NumberHelper.isNumeric("a-z"));
        assertFalse(NumberHelper.isNumeric("5-a"));
    }

    /**
     * Test the {@link NumberHelper#tokenize(String)} method
     */
    @Test
    public void tokenize() {
        assertEquals(Collections.singletonList(new NumberToken("1", NumberTokenType.NUMBER)),
                NumberHelper.tokenize("1"));

        assertEquals(Arrays.asList(
                new NumberToken("1", NumberTokenType.NUMBER),
                new NumberToken("-", NumberTokenType.SEPARATOR),
                new NumberToken("10", NumberTokenType.NUMBER),
                new NumberToken(",", NumberTokenType.SEPARATOR),
                new NumberToken("2", NumberTokenType.NUMBER),
                new NumberToken("-", NumberTokenType.SEPARATOR),
                new NumberToken("100", NumberTokenType.NUMBER)),
                NumberHelper.tokenize("1-10,2-100"));

        assertEquals(Arrays.asList(
                new NumberToken("42a", NumberTokenType.NUMBER),
                new NumberToken("-", NumberTokenType.SEPARATOR),
                new NumberToken("tt0815", NumberTokenType.NUMBER),
                new NumberToken(",", NumberTokenType.SEPARATOR),
                new NumberToken("ll47uu", NumberTokenType.NUMBER),
                new NumberToken("&", NumberTokenType.SEPARATOR),
                new NumberToken("o11", NumberTokenType.NUMBER)),
                NumberHelper.tokenize("42a -  tt0815 ,  ll47uu &o11"));
    }
}
