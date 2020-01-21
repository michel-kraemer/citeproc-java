package de.undercouch.citeproc.helper;

import java.math.BigInteger;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * <p>Performs locale-sensitive comparison of {@link String}s with numbers
 * to produce a natural order that appears logical to humans.</p>
 *
 * <p>Examples:</p>
 * <pre>
 * compare("Hello", "Hello")    =  0
 * compare("10", "2")           =  1
 * compare("10th", "2nd")       =  1
 * compare("File 10", "File 2") =  1
 * compare("Manana", "Mañana")  = -1
 * </pre>
 *
 * <p>The implementation is loosely based on the Alphanum algorithm by
 * <a href="http://www.davekoelle.com/alphanum.html">Dave Koelle</a> and the
 * <a href="http://www.davekoelle.com/files/AlphanumComparator.java">Java
 * implementation</a> released under the MIT license. However, it has been
 * extended to use a {@link Collator} for locale-sensitive comparison, and it
 * is also able to compare arbitrarily large numbers.</p>
 *
 * @author Michel Kraemer
 */
public class AlphanumComparator implements Comparator<CharSequence> {
    private final Collator collator;

    /**
     * Create a new comparator for the given locale
     * @param locale the locale
     */
    public AlphanumComparator(Locale locale) {
        collator = Collator.getInstance(locale);
    }

    /**
     * Find the next number in the given character sequence
     * @param s the character sequence
     * @param start the index of the character where the search should start
     * @return the index of the first digit of the number found or the length
     * of the character sequence if no number was found
     */
    private int findNextNumber(CharSequence s, int start) {
        while (start < s.length() && !Character.isDigit(s.charAt(start))) {
            ++start;
        }
        return start;
    }

    /**
     * Find the end of a number in the given character sequence
     * @param s the character sequence
     * @param start the index of the character where the search should start
     * @return the index of the first character that is not a digit or the
     * length of the character sequence if there is no other character
     */
    private int findNumberEnd(CharSequence s, int start) {
        while (start < s.length() && Character.isDigit(s.charAt(start))) {
            ++start;
        }
        return start;
    }

    @Override
    public int compare(CharSequence a, CharSequence b) {
        int lastEndA = 0;
        int lastEndB = 0;

        while (lastEndA < a.length() && lastEndB < b.length()) {
            int startA = findNextNumber(a, lastEndA);
            int startB = findNextNumber(b, lastEndB);
            if (startA == a.length() || startB == b.length()) {
                // no more numbers remaining
                break;
            }

            // compare text
            if (lastEndA < startA || lastEndB < startB) {
                CharSequence subA = a.subSequence(lastEndA, startA);
                CharSequence subB = b.subSequence(lastEndB, startB);
                int c = collator.compare(subA, subB);
                if (c != 0) {
                    return c;
                }
            }

            // compare number
            int endA = findNumberEnd(a, startA);
            int endB = findNumberEnd(b, startB);
            CharSequence subA = a.subSequence(startA, endA);
            CharSequence subB = b.subSequence(startB, endB);
            if (subA.length() > 9 || subB.length() > 9) {
                BigInteger ia = new BigInteger(subA.toString());
                BigInteger ib = new BigInteger(subB.toString());
                int c = ia.compareTo(ib);
                if (c != 0) {
                    return c;
                }
            } else {
                Long ia = Long.parseLong(subA.toString());
                Long ib = Long.parseLong(subB.toString());
                int c = ia.compareTo(ib);
                if (c != 0) {
                    return c;
                }
            }

            lastEndA = endA;
            lastEndB = endB;
        }

        if (lastEndA == 0 && lastEndB == 0) {
            return collator.compare(a, b);
        }

        if (lastEndA < a.length() || lastEndB < b.length()) {
            CharSequence subA = a.subSequence(lastEndA, a.length());
            CharSequence subB = b.subSequence(lastEndB, b.length());
            return collator.compare(subA, subB);
        }

        return 0;
    }
}
