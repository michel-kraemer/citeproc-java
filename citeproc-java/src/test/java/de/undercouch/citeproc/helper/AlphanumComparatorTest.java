package de.undercouch.citeproc.helper;

import org.junit.Test;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link AlphanumComparator}
 * @author Michel Kraemer
 */
public class AlphanumComparatorTest {
    /**
     * Test the {@link AlphanumComparator#compare(CharSequence, CharSequence)} method
     */
    @Test
    public void compare() {
        AlphanumComparator c = new AlphanumComparator(new Locale("es"));

        assertEquals(0, c.compare("Hello", "Hello"));
        assertEquals(0, c.compare("Hello World", "Hello World"));

        assertEquals(-1, c.compare("Hello", "World"));
        assertEquals(1, c.compare("World", "Hello"));
        assertEquals(1, c.compare("World 1", "World"));
        assertEquals(-1, c.compare("World", "World 1"));
        assertEquals(-1, c.compare("Hello 1", "World"));
        assertEquals(0, c.compare("Hello 1", "Hello 1"));
        assertEquals(0, c.compare("Hello 1 World", "Hello 1 World"));
        assertEquals(-1, c.compare("Hello 1 World", "Hello 1 Zoo"));
        assertEquals(1, c.compare("Hello 10 World", "Hello 1 Zoo"));

        assertEquals(1, c.compare("Hello 1 World 10", "Hello 1 World 2"));
        assertEquals(1, c.compare("Hello 1 World 10 b", "Hello 1 World 10 a"));

        assertEquals(1, c.compare("axa 2", "aña 10"));
        assertEquals(-1, c.compare("aña 10", "axi 2"));
        assertEquals(-1, c.compare("ana 10", "aña 10"));

        assertEquals(1, c.compare("Tile 2", "File 10"));
        assertEquals(1, c.compare("Zoo 2", "File 10"));

        assertEquals(-1, c.compare("File 2", "File 10"));
        assertEquals(1, c.compare("File 10", "File 2"));
        assertEquals(-1, c.compare("File2", "File10"));
        assertEquals(1, c.compare("File10", "File2"));

        assertEquals(-1, c.compare("2", "File 10"));
        assertEquals(-1, c.compare("10", "File 2"));

        assertEquals(-1, c.compare("2", "10 File"));
        assertEquals(1, c.compare("10", "2 File"));

        assertEquals(-1, c.compare("2", "2 File"));
        assertEquals(1, c.compare("1 File", "1"));

        assertEquals(-1, c.compare("2", "File"));
        assertEquals(1, c.compare("File", "1"));

        assertEquals(-1, c.compare("Hello 2", "Hello File 10"));
        assertEquals(-1, c.compare("Hello 10", "Hello File 2"));

        assertEquals(-1, c.compare("2", "10"));
        assertEquals(1, c.compare("2", "1"));

        assertEquals(1, c.compare("-2", "2"));
        assertEquals(1, c.compare("-0.5", "-0.1"));
        assertEquals(1, c.compare("-0.5", "-0.1e1"));

        assertEquals(1, c.compare("1-2", "1-1"));
        assertEquals(1, c.compare("1 -2", "1 -1"));

        assertEquals(-1, c.compare("Manana", "Mañana"));
    }

    /**
     * Test if {@link java.math.BigInteger}s can be compared correctly
     */
    @Test
    public void bigInt() {
        AlphanumComparator c = new AlphanumComparator(new Locale("es"));
        assertEquals(-1, c.compare("Hello 24", "Hello 7846785478595743"));
        assertEquals(-1, c.compare("Hello 7846785478595742", "Hello 7846785478595743"));
    }

    /**
     * Test sorting using the {@link AlphanumComparator}
     */
    @Test
    public void sort() {
        List<String> words = new ArrayList<>();
        words.add("a");
        words.add("mña 10");
        words.add("mña 2");
        words.add("mn");
        words.add("mnz");
        words.add("mz");
        words.add("z");

        List<String> expected1 = new ArrayList<>();
        expected1.add("a");
        expected1.add("mn");
        expected1.add("mnz");
        expected1.add("mz");
        expected1.add("mña 10");
        expected1.add("mña 2");
        expected1.add("z");

        List<String> expected2 = new ArrayList<>();
        expected2.add("a");
        expected2.add("mn");
        expected2.add("mnz");
        expected2.add("mña 10");
        expected2.add("mña 2");
        expected2.add("mz");
        expected2.add("z");

        List<String> expected3 = new ArrayList<>();
        expected3.add("a");
        expected3.add("mn");
        expected3.add("mnz");
        expected3.add("mña 2");
        expected3.add("mña 10");
        expected3.add("mz");
        expected3.add("z");

        List<String> sorted1 = new ArrayList<>(words);
        Collections.sort(sorted1);
        assertEquals(expected1, sorted1);

        Collator collator = Collator.getInstance(new Locale("es"));
        List<String> sorted2 = new ArrayList<>(words);
        sorted2.sort(collator);
        assertEquals(expected2, sorted2);

        AlphanumComparator c = new AlphanumComparator(new Locale("es"));
        List<String> sorted3 = new ArrayList<>(words);
        sorted3.sort(c);
        assertEquals(expected3, sorted3);
    }
}
