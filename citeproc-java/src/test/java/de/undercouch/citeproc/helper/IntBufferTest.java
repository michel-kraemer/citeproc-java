package de.undercouch.citeproc.helper;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link IntBuffer}
 * @author Michel Kraemer
 */
public class IntBufferTest {
    /**
     * Test {@link IntBuffer#length()}
     */
    @Test
    public void length() {
        assertEquals(0, new IntBuffer().length());
        assertEquals(5, new IntBuffer(0, 1, 2, 3, 4).length());
    }

    /**
     * Test {@link IntBuffer#isEmpty()}
     */
    @Test
    public void isEmpty() {
        assertTrue(new IntBuffer().isEmpty());
        assertFalse(new IntBuffer(1).isEmpty());
    }

    /**
     * Test {@link IntBuffer#get(int)} 
     */
    @Test
    public void get() {
        IntBuffer b = new IntBuffer(0, 5, 2, 1, 4);
        assertEquals(2, b.get(2));
        assertEquals(4, b.get(4));
    }

    /**
     * Test {@link IntBuffer#toArray()} 
     */
    @Test
    public void toArray() {
        assertArrayEquals(new int[0], new IntBuffer().toArray());
        assertArrayEquals(new int[] { 0, 5, 2, 1, 4 },
                new IntBuffer(0, 5, 2, 1, 4).toArray());
    }

    /**
     * Test {@link IntBuffer#append(int)}
     */
    @Test
    public void append() {
        IntBuffer b1 = new IntBuffer(0, 1);
        IntBuffer b2 = b1.append(2);
        assertArrayEquals(new int[] { 0, 1 }, b1.toArray());
        assertArrayEquals(new int[] { 0, 1, 2 }, b2.toArray());
        assertNotSame(b1, b2);
    }

    /**
     * Test {@link IntBuffer#indexOf(int)}
     */
    @Test
    public void indexOf() {
        assertEquals(-1, new IntBuffer().indexOf(0));
        assertEquals(-1, new IntBuffer(1).indexOf(0));
        assertEquals(-1, new IntBuffer(2, 3).indexOf(0));
        assertEquals(0, new IntBuffer(0).indexOf(0));
        assertEquals(0, new IntBuffer(1).indexOf(1));
        assertEquals(0, new IntBuffer(2, 3).indexOf(2));
        assertEquals(1, new IntBuffer(2, 3).indexOf(3));
    }
}
