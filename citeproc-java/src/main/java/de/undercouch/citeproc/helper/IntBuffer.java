package de.undercouch.citeproc.helper;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * An immutable buffer of integers
 * @author Michel Kraemer
 */
public class IntBuffer {
    private final int[] buffer;

    /**
     * Create an empty buffer
     */
    public IntBuffer() {
        buffer = ArrayUtils.EMPTY_INT_ARRAY;
    }

    /**
     * Create a buffer that contains the given integers. Creates a copy of
     * the given array.
     * @param ints the array of integers to copy
     */
    public IntBuffer(int... ints) {
        buffer = Arrays.copyOf(ints, ints.length);
    }

    /**
     * Create a copy of the given buffer and append an integer value
     * @param buffer the buffer to copy
     * @param next the integer value to append
     */
    private IntBuffer(int[] buffer, int next) {
        this.buffer = Arrays.copyOf(buffer, buffer.length + 1);
        this.buffer[this.buffer.length - 1] = next;
    }

    /**
     * Get the buffer's length
     * @return the length
     */
    public int length() {
        return buffer.length;
    }

    /**
     * Return {@code true} if {@link #length()} is {@code 0}
     * @return {@code true} if {@link #length()} is {@code 0}, otherwise
     * {@code false}
     */
    public boolean isEmpty() {
        return buffer.length == 0;
    }

    /**
     * Get the integer value at the specified index
     * @param index the index
     * @return the integer value at the specified index
     */
    public int get(int index) {
        return buffer[index];
    }

    /**
     * Find the index of the given integer value in the buffer
     * @param i the integer value
     * @return the index of the value in the buffer or {@code -1} if the
     * buffer does not contain the value
     */
    public int indexOf(int i) {
        return ArrayUtils.indexOf(buffer, i);
    }

    /**
     * Return a copy of this buffer as an array
     * @return the array
     */
    public int[] toArray() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    /**
     * Create a new buffer that contains the contents of this one plus an
     * additional integer value
     * @param i the integer value to append
     * @return the new buffer
     */
    public IntBuffer append(int i) {
        return new IntBuffer(buffer, i);
    }
}
