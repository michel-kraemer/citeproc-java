package de.undercouch.citeproc.bibtex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the page parser
 * @author Michel Kraemer
 */
public class PageParserTest {
    /**
     * Tests a single page
     */
    @Test
    public void singlePage() {
        PageRange pr = PageParser.parse("10");
        assertEquals(Integer.valueOf(1), pr.getNumberOfPages());
        assertFalse(pr.isMultiplePages());
    }

    /**
     * Tests page ranges
     */
    @Test
    public void simplePageRange() {
        PageRange pr = PageParser.parse("10-20");
        assertEquals("10-20", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(11), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10 - 20");
        assertEquals("10-20", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(11), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10--20");
        assertEquals("10-20", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(11), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10---20");
        assertEquals("10-20", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(11), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10\u201320");
        assertEquals("10-20", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(11), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests two page ranges separated by a comma
     */
    @Test
    public void twoPageRanges() {
        PageRange pr = PageParser.parse("10-20,30--40");
        assertEquals("10-20,30-40", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(22), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10 -  20 ,30 --40");
        assertEquals("10-20,30-40", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(22), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests a complex page range
     */
    @Test
    public void complex() {
        PageRange pr = PageParser.parse("10-20,30--40,45,50\u201355,5");
        assertEquals("10-20,30-40,45,50-55,5", pr.getLiteral());
        assertEquals("5", pr.getPageFirst());
        assertEquals(Integer.valueOf(30), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests page range containing an unknown page
     */
    @Test
    public void fromToUnknown() {
        PageRange pr = PageParser.parse("10-??");
        assertEquals("10-??", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("10 - ??");
        assertEquals("10 - ??", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests page range containing unknown pages
     */
    @Test
    public void unknownRange() {
        PageRange pr = PageParser.parse("??-??");
        assertEquals("??-??", pr.getLiteral());
        assertEquals("??", pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        pr = PageParser.parse("?? - ??");
        assertEquals("?? - ??", pr.getLiteral());
        assertEquals("??", pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests unknown page
     */
    @Test
    public void unknownPage() {
        PageRange pr = PageParser.parse("??");
        assertEquals("??", pr.getLiteral());
        assertEquals("??", pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertFalse(pr.isMultiplePages());
    }

    /**
     * Tests if a pseudo page range is rendered as a single page
     */
    @Test
    public void singlePageRange() {
        PageRange pr = PageParser.parse("10-10");
        assertEquals("10", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(1), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests if two pages can be parsed
     */
    @Test
    public void twoPages() {
        PageRange pr = PageParser.parse("10,11");
        assertEquals("10,11", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(2), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());

        PageParser.parse("10, 11");
        assertEquals("10,11", pr.getLiteral());
        assertEquals("10", pr.getPageFirst());
        assertEquals(Integer.valueOf(2), pr.getNumberOfPages());
        assertTrue(pr.isMultiplePages());
    }

    /**
     * Tests if two pages can be parsed
     */
    @Test
    public void literal() {
        PageRange pr = PageParser.parse("A page");
        assertEquals("A page", pr.getLiteral());
        assertNull(pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertFalse(pr.isMultiplePages());

        pr = PageParser.parse("A,,page");
        assertEquals("A,,page", pr.getLiteral());
        assertNull(pr.getPageFirst());
        assertNull(pr.getNumberOfPages());
        assertFalse(pr.isMultiplePages());
    }
}
