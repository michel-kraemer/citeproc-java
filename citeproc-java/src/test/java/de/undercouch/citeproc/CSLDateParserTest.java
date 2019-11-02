package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLDate;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests {@link CSLDateParser}
 * @author Michel Kraemer
 */
public class CSLDateParserTest {
    /**
     * Tests if simple dates can be parsed correctly
     */
    @Test
    public void simple() {
        CSLDateParser parser = new CSLDateParser();
        CSLDate date = parser.parse("2013-11-17");
        assertArrayEquals(new int[][] { new int[] { 2013, 11, 17 } }, date.getDateParts());

        date = parser.parse("2013-11");
        assertArrayEquals(new int[][] { new int[] { 2013, 11 } }, date.getDateParts());

        date = parser.parse("2013");
        assertArrayEquals(new int[][] { new int[] { 2013 } }, date.getDateParts());

        date = parser.parse("");
        assertArrayEquals(new int[][] { new int[0] }, date.getDateParts());
    }

    /**
     * Tests if dates with literal month names can be parsed correctly
     */
    @Test
    public void alpha() {
        CSLDateParser parser = new CSLDateParser();
        CSLDate date = parser.parse("November 2013");
        assertArrayEquals(new int[][] { new int[] { 2013, 11 } }, date.getDateParts());
    }

    /**
     * Tests if dates with a slash can be parsed correctly
     */
    @Test
    public void slash() {
        CSLDateParser parser = new CSLDateParser();
        CSLDate date = parser.parse("2013/11/17");
        assertArrayEquals(new int[][] { new int[] { 2013, 11, 17 } }, date.getDateParts());

        date = parser.parse("11/17/2013");
        assertArrayEquals(new int[][] { new int[] { 2013, 11, 17 } }, date.getDateParts());
    }
}
