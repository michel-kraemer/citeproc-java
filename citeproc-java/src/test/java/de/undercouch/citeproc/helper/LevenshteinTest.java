package de.undercouch.citeproc.helper;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link Levenshtein} implementation
 * @author Michel Kraemer
 */
public class LevenshteinTest {
    /**
     * Tests the {@link Levenshtein#findMinimum(java.util.Collection, CharSequence)} method
     */
    @Test
    public void findMinimum() {
        List<String> ss = Arrays.asList("Holla", "World", "Hello", "Hippo", "Hiplo");
        CharSequence min = Levenshtein.findMinimum(ss, "Hillo");
        assertEquals("Hello", min);
    }

    /**
     * Tests the {@link Levenshtein#findMinimum(Collection, CharSequence, int, int)} method
     */
    @Test
    public void findThreeMinumums() {
        List<String> ss = Arrays.asList("Holla", "World", "Hello", "Hippo", "Hiplo");
        Collection<String> min = Levenshtein.findMinimum(ss, "Hillo", 3, 5);
        assertEquals(Arrays.asList("Hello", "Hiplo", "Holla"), min);
    }

    /**
     * Tests the {@link Levenshtein#findMinimum(Collection, CharSequence, int, int)} method
     */
    @Test
    public void findMinumumsThreshold() {
        List<String> ss = Arrays.asList("Holla", "World", "Hello", "Hippo", "Hiplo");
        Collection<String> min = Levenshtein.findMinimum(ss, "Hillo", 5, 2);
        assertEquals(Arrays.asList("Hello", "Hiplo"), min);
    }
}
