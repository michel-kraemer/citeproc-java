package de.undercouch.citeproc.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses {@link LevenshteinDistance#apply(CharSequence, CharSequence)}
 * to calculate the edit distance between two strings. Provides useful helper
 * methods to traverse a set of strings and select the most similar ones
 * to a given input string.
 * @author Michel Kraemer
 */
public class Levenshtein {
    private static class Item<T> implements Comparable<Item<T>> {
        private final T str;
        private final int distance;

        public Item(T str, int distance) {
            this.str = str;
            this.distance = distance;
        }

        @Override
        public int compareTo(Item<T> o) {
            return Integer.compare(distance, o.distance);
        }
    }

    /**
     * Searches the given collection of strings and returns the string that
     * has the lowest Levenshtein distance to a given second string <code>t</code>.
     * If the collection contains multiple strings with the same distance to
     * <code>t</code> only the first one will be returned.
     * @param <T> the type of the strings in the given collection
     * @param ss the collection to search
     * @param t the second string
     * @return the string with the lowest Levenshtein distance
     */
    public static <T extends CharSequence> T findMinimum(Collection<T> ss,
            CharSequence t) {
        int min = Integer.MAX_VALUE;
        T result = null;
        for (T s : ss) {
            int d = LevenshteinDistance.getDefaultInstance().apply(s, t);
            if (d < min) {
                min = d;
                result = s;
            }
        }
        return result;
    }

    /**
     * Searches the given collection of strings and returns a collection of at
     * most <code>n</code> strings that have the lowest Levenshtein distance
     * to a given string <code>t</code>. The returned collection will be
     * sorted according to the distance with the string with the lowest
     * distance at the first position.
     * @param <T> the type of the strings in the given collection
     * @param ss the collection to search
     * @param t the string to compare to
     * @param n the maximum number of strings to return
     * @param threshold a threshold for individual item distances. Only items
     * with a distance below this threshold will be included in the result.
     * @return the strings with the lowest Levenshtein distance
     */
    public static <T extends CharSequence> Collection<T> findMinimum(
            Collection<T> ss, CharSequence t, int n, int threshold) {
        LinkedList<Item<T>> result = new LinkedList<>();
        for (T s : ss) {
            int d = LevenshteinDistance.getDefaultInstance().apply(s, t);
            if (d < threshold) {
                result.offer(new Item<>(s, d));

                if (result.size() > n + 10) {
                    // resort, but not too often
                    Collections.sort(result);
                    while (result.size() > n) result.removeLast();
                }
            }
        }

        Collections.sort(result);
        while (result.size() > n) result.removeLast();

        List<T> arr = new ArrayList<>(n);
        for (Item<T> i : result) {
            arr.add(i.str);
        }
        return arr;
    }

    /**
     * Searches the given collection of strings and returns a collection of
     * strings similar to a given string <code>t</code>. Uses reasonable default
     * values for human-readable strings. The returned collection will be
     * sorted according to their similarity with the string with the best
     * match at the first position.
     * @param <T> the type of the strings in the given collection
     * @param ss the collection to search
     * @param t the string to compare to
     * @return a collection with similar strings
     */
    public static <T extends CharSequence> Collection<T> findSimilar(
            Collection<T> ss, CharSequence t) {
        // look for strings prefixed by 't'
        Collection<T> result = new LinkedHashSet<>();
        for (T s : ss) {
            if (StringUtils.startsWithIgnoreCase(s, t)) {
                result.add(s);
            }
        }

        // find strings according to their levenshtein distance
        Collection<T> mins = findMinimum(ss, t, 5, Math.min(t.length() - 1, 7));
        result.addAll(mins);

        return result;
    }
}
