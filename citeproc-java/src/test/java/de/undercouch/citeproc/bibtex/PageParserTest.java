package de.undercouch.citeproc.bibtex;

import org.junit.Test;

import java.util.function.Consumer;

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
        PageRanges prs = PageParser.parse("10");
        assertEquals(Integer.valueOf(1), prs.getNumberOfPages());
        assertFalse(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals(Integer.valueOf(1), pr0.getNumberOfPages());
        assertEquals("10", pr0.getPageFirst());
        assertEquals("10", pr0.getPageLast());
        assertFalse(pr0.isMultiplePages());
    }

    /**
     * Tests page ranges
     */
    @Test
    public void simplePageRange() {
        Consumer<PageRanges> checkPageRanges = prs -> {
            assertEquals("10-20", prs.getLiteral());
            assertEquals("10", prs.getPageFirst());
            assertEquals(Integer.valueOf(11), prs.getNumberOfPages());
            assertTrue(prs.isMultiplePages());

            assertEquals(1, prs.size());
            PageRange pr0 = prs.get(0);
            assertEquals("10-20", pr0.getLiteral());
            assertEquals("10", pr0.getPageFirst());
            assertEquals("20", pr0.getPageLast());
            assertEquals(Integer.valueOf(11), pr0.getNumberOfPages());
            assertTrue(pr0.isMultiplePages());
        };

        checkPageRanges.accept(PageParser.parse("10-20"));
        checkPageRanges.accept(PageParser.parse("10 - 20"));
        checkPageRanges.accept(PageParser.parse("10--20"));
        checkPageRanges.accept(PageParser.parse("10---20"));
        checkPageRanges.accept(PageParser.parse("10\u201320"));
    }

    /**
     * Tests two page ranges separated by a comma
     */
    @Test
    public void twoPageRanges() {
        Consumer<PageRanges> checkPageRanges = prs -> {
            assertEquals("10-20, 30-40", prs.getLiteral());
            assertEquals("10", prs.getPageFirst());
            assertEquals(Integer.valueOf(22), prs.getNumberOfPages());
            assertTrue(prs.isMultiplePages());

            assertEquals(2, prs.size());
            PageRange pr0 = prs.get(0);
            assertEquals("10-20", pr0.getLiteral());
            assertEquals("10", pr0.getPageFirst());
            assertEquals("20", pr0.getPageLast());
            assertEquals(Integer.valueOf(11), pr0.getNumberOfPages());
            assertTrue(pr0.isMultiplePages());

            PageRange pr1 = prs.get(1);
            assertEquals("30-40", pr1.getLiteral());
            assertEquals("30", pr1.getPageFirst());
            assertEquals("40", pr1.getPageLast());
            assertEquals(Integer.valueOf(11), pr1.getNumberOfPages());
            assertTrue(pr1.isMultiplePages());
        };

        checkPageRanges.accept(PageParser.parse("10-20,30--40"));
        checkPageRanges.accept(PageParser.parse("10 -  20 ,30 --40"));
    }

    /**
     * Tests a complex page range
     */
    @Test
    public void complex() {
        PageRanges prs = PageParser.parse("10-20, 30--40, 45, 50\u201355, 5");
        assertEquals("10-20, 30-40, 45, 50-55, 5", prs.getLiteral());
        assertEquals("5", prs.getPageFirst());
        assertEquals(Integer.valueOf(30), prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        PageRange pr0 = prs.get(0);
        assertEquals("10-20", pr0.getLiteral());
        assertEquals("10", pr0.getPageFirst());
        assertEquals("20", pr0.getPageLast());
        assertEquals(Integer.valueOf(11), pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());

        PageRange pr1 = prs.get(1);
        assertEquals("30-40", pr1.getLiteral());
        assertEquals("30", pr1.getPageFirst());
        assertEquals("40", pr1.getPageLast());
        assertEquals(Integer.valueOf(11), pr1.getNumberOfPages());
        assertTrue(pr1.isMultiplePages());

        PageRange pr2 = prs.get(2);
        assertEquals("45", pr2.getLiteral());
        assertEquals("45", pr2.getPageFirst());
        assertEquals("45", pr2.getPageLast());
        assertEquals(Integer.valueOf(1), pr2.getNumberOfPages());
        assertFalse(pr2.isMultiplePages());

        PageRange pr3 = prs.get(3);
        assertEquals("50-55", pr3.getLiteral());
        assertEquals("50", pr3.getPageFirst());
        assertEquals("55", pr3.getPageLast());
        assertEquals(Integer.valueOf(6), pr3.getNumberOfPages());
        assertTrue(pr3.isMultiplePages());

        PageRange pr4 = prs.get(4);
        assertEquals("5", pr4.getLiteral());
        assertEquals("5", pr4.getPageFirst());
        assertEquals("5", pr4.getPageLast());
        assertEquals(Integer.valueOf(1), pr4.getNumberOfPages());
        assertFalse(pr4.isMultiplePages());
    }

    /**
     * Tests page range containing an unknown page
     */
    @Test
    public void fromToUnknown() {
        PageRanges prs = PageParser.parse("10-??");
        assertEquals("10-??", prs.getLiteral());
        assertEquals("10", prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals("10-??", pr0.getLiteral());
        assertEquals("10", pr0.getPageFirst());
        assertEquals("??", pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());

        prs = PageParser.parse("10 - ??");
        assertEquals("10 - ??", prs.getLiteral());
        assertEquals("10", prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        assertEquals(1, prs.size());
        pr0 = prs.get(0);
        assertEquals("10 - ??", pr0.getLiteral());
        assertEquals("10", pr0.getPageFirst());
        assertEquals("??", pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());
    }

    /**
     * Tests page range containing unknown pages
     */
    @Test
    public void unknownRange() {
        PageRanges prs = PageParser.parse("??-??");
        assertEquals("??-??", prs.getLiteral());
        assertEquals("??", prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals("??-??", pr0.getLiteral());
        assertEquals("??", pr0.getPageFirst());
        assertEquals("??", pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());

        prs = PageParser.parse("?? - ??");
        assertEquals("?? - ??", prs.getLiteral());
        assertEquals("??", prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        assertEquals(1, prs.size());
        pr0 = prs.get(0);
        assertEquals("?? - ??", pr0.getLiteral());
        assertEquals("??", pr0.getPageFirst());
        assertEquals("??", pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());
    }

    /**
     * Tests unknown page
     */
    @Test
    public void unknownPage() {
        PageRanges prs = PageParser.parse("??");
        assertEquals("??", prs.getLiteral());
        assertEquals("??", prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertFalse(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals("??", pr0.getLiteral());
        assertEquals("??", pr0.getPageFirst());
        assertEquals("??", pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertFalse(pr0.isMultiplePages());
    }

    /**
     * Tests if a pseudo page range is rendered as a single page
     */
    @Test
    public void singlePageRange() {
        PageRanges prs = PageParser.parse("10-10");
        assertEquals("10", prs.getLiteral());
        assertEquals("10", prs.getPageFirst());
        assertEquals(Integer.valueOf(1), prs.getNumberOfPages());
        assertTrue(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals("10", pr0.getLiteral());
        assertEquals("10", pr0.getPageFirst());
        assertEquals("10", pr0.getPageLast());
        assertEquals(Integer.valueOf(1), pr0.getNumberOfPages());
        assertTrue(pr0.isMultiplePages());
    }

    /**
     * Tests if two pages can be parsed
     */
    @Test
    public void twoPages() {
        Consumer<PageRanges> checkPageRanges = prs -> {
            assertEquals("10, 11", prs.getLiteral());
            assertEquals("10", prs.getPageFirst());
            assertEquals(Integer.valueOf(2), prs.getNumberOfPages());
            assertTrue(prs.isMultiplePages());

            assertEquals(2, prs.size());
            PageRange pr0 = prs.get(0);
            assertEquals("10", pr0.getLiteral());
            assertEquals("10", pr0.getPageFirst());
            assertEquals("10", pr0.getPageLast());
            assertEquals(Integer.valueOf(1), pr0.getNumberOfPages());
            assertFalse(pr0.isMultiplePages());

            PageRange pr1 = prs.get(1);
            assertEquals("11", pr1.getLiteral());
            assertEquals("11", pr1.getPageFirst());
            assertEquals("11", pr1.getPageLast());
            assertEquals(Integer.valueOf(1), pr1.getNumberOfPages());
            assertFalse(pr1.isMultiplePages());
        };

        checkPageRanges.accept(PageParser.parse("10,11"));
        checkPageRanges.accept(PageParser.parse("10, 11"));
    }

    /**
     * Tests if two pages can be parsed
     */
    @Test
    public void literal() {
        PageRanges prs = PageParser.parse("A page");
        assertEquals("A page", prs.getLiteral());
        assertNull(prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertFalse(prs.isMultiplePages());

        assertEquals(1, prs.size());
        PageRange pr0 = prs.get(0);
        assertEquals("A page", pr0.getLiteral());
        assertNull(pr0.getPageFirst());
        assertNull(pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertFalse(pr0.isMultiplePages());

        prs = PageParser.parse("A,,page");
        assertEquals("A,,page", prs.getLiteral());
        assertNull(prs.getPageFirst());
        assertNull(prs.getNumberOfPages());
        assertFalse(prs.isMultiplePages());

        assertEquals(1, prs.size());
        pr0 = prs.get(0);
        assertEquals("A,,page", pr0.getLiteral());
        assertNull(pr0.getPageFirst());
        assertNull(pr0.getPageLast());
        assertNull(pr0.getNumberOfPages());
        assertFalse(pr0.isMultiplePages());
    }
}
