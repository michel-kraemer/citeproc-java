package de.undercouch.citeproc.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to Strings
 * @author Michel Kraemer
 */
public class StringHelper {
    /**
     * Hexadecimal characters
     */
    private final static char[] HEX_DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Words that should not converted to title-case
     */
    private final static String[] TITLECASE_STOPWORDS = {
            "a", "an", "and", "as", "at", "but", "by", "down", "for", "from",
            "in", "into", "nor", "of", "on", "onto", "or", "over", "so", "the",
            "till", "to", "up", "via", "with", "without", "yet"
    };

    /**
     * Sanitizes a string so it can be used as an identifier
     * @param s the string to sanitize
     * @return the sanitized string
     */
    public static String sanitize(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\u00c0':
                case '\u00c1':
                case '\u00c3':
                case '\u00c4':
                    sb.append('A');
                    break;

                case '\u00c8':
                case '\u00c9':
                case '\u00cb':
                    sb.append('E');
                    break;

                case '\u00cc':
                case '\u00cd':
                case '\u00cf':
                    sb.append('I');
                    break;

                case '\u00d2':
                case '\u00d3':
                case '\u00d5':
                case '\u00d6':
                    sb.append('O');
                    break;

                case '\u00d9':
                case '\u00da':
                case '\u00dc':
                    sb.append('U');
                    break;

                case '\u00e0':
                case '\u00e1':
                case '\u00e3':
                case '\u00e4':
                    sb.append('a');
                    break;

                case '\u00e8':
                case '\u00e9':
                case '\u00eb':
                    sb.append('e');
                    break;

                case '\u00ec':
                case '\u00ed':
                case '\u00ef':
                    sb.append('i');
                    break;

                case '\u00f2':
                case '\u00f3':
                case '\u00f6':
                case '\u00f5':
                    sb.append('o');
                    break;

                case '\u00f9':
                case '\u00fa':
                case '\u00fc':
                    sb.append('u');
                    break;

                case '\u00d1':
                    sb.append('N');
                    break;

                case '\u00f1':
                    sb.append('n');
                    break;

                case '\u010c':
                    sb.append('C');
                    break;

                case '\u0160':
                    sb.append('S');
                    break;

                case '\u017d':
                    sb.append('Z');
                    break;

                case '\u010d':
                    sb.append('c');
                    break;

                case '\u0161':
                    sb.append('s');
                    break;

                case '\u017e':
                    sb.append('z');
                    break;

                case '\u00df':
                    sb.append("ss");
                    break;

                default:
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                            (c >= '0' && c <= '9')) {
                        sb.append(c);
                    } else {
                        sb.append('_');
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Escapes characters in the given string according to Java rules
     * @param s the string to escape
     * @return the escpaped string
     */
    public static String escapeJava(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(Math.min(2, s.length() * 3 / 2));
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\b') {
                sb.append("\\b");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\f') {
                sb.append("\\f");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c < 32 || c > 0x7f) {
                sb.append("\\u");
                sb.append(hex4(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts the given character to a four-digit hexadecimal string
     * @param c the character to convert
     * @return the string
     */
    private static String hex4(char c) {
        char[] r = new char[] { '0', '0', '0', '0' };
        int i = 3;
        while (c > 0) {
            r[i] = HEX_DIGITS[c & 0xF];
            c >>>= 4;
            --i;
        }
        return new String(r);
    }

    /**
     * <p>Calculates how many characters overlap between {@code a} and {@code b},
     * i.e. how many characters at the end of {@code a} are equal to the ones
     * at the beginning of {@code b}.</p>
     *
     * <p>Examples:</p>
     * <pre>
     * overlap("abcd", "cdef")     = 2
     * overlap("abcd", "xyz")      = 0
     * overlap("a", "a")           = 1
     * overlap("ab", "b")          = 1
     * overlap("abcd", "bcdefg")   = 3
     * overlap("", "a")            = 0
     * overlap("a", "")            = 0
     * </pre>
     *
     * @param a the first string
     * @param b the second string
     * @return the number of overlapping characters
     */
    public static int overlap(CharSequence a, CharSequence b) {
        if (a == null || b == null || a.length() == 0 || b.length() == 0) {
            return 0;
        }

        int start = Math.max(0, a.length() - b.length());
        for (int i = start; i < a.length(); ++i) {
            int j = 0;
            for (; j < b.length() && i + j < a.length(); ++j) {
                if (a.charAt(i + j) != b.charAt(j)) {
                    break;
                }
            }

            if (i + j == a.length()) {
                return j;
            }
        }

        return 0;
    }

    /**
     * Splits a string along well-known delimiters for {@link #toTitleCase(String)}
     * @param str the string to split
     * @return the parts of the string
     */
    private static List<String> split(String str) {
        List<String> result = new ArrayList<>();
        int s = 0;
        for (int i = 0; i <= str.length(); ++i) {
            if (i == str.length()) {
                result.add(str.substring(s, i));
                break;
            }
            char c = str.charAt(i);
            if (Character.isSpaceChar(c) || c == ':' || c == '\u2014' ||
                    c == '\u2013' || c == '-') {
                result.add(str.substring(s, i));
                result.add(str.substring(i, i + 1));
                s = i + 1;
            }
        }
        return result;
    }

    /**
     * Check if the given string equals on the {@link #TITLECASE_STOPWORDS}
     * @param w the word
     * @return {@code true} if the given string is a stop word
     */
    private static boolean isStopWord(String w) {
        for (String titlecaseStopword : TITLECASE_STOPWORDS) {
            if (w.equalsIgnoreCase(titlecaseStopword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares the element at position {@code i} from the given list {@code l}
     * with a string {@code other}. Also makes sure {@code i} is within the
     * string's bounds. If it is not, the method returns {@code false}.
     * @param l the list
     * @param i the index
     * @param other the string to compare to
     * @return {@code true} if the element equals the string
     */
    private static boolean safeEquals(List<String> l, int i, String other) {
        return i >= 0 && i < l.size() && l.get(i).equals(other);
    }

    /**
     * Check if all characters in the given string are uppercase
     * @param s the string
     * @return {@code true} if the string contains only uppercase characters
     */
    private static boolean isAllUppercase(String s) {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c < 'A' || c > 'Z') {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string should be capitalized
     * @param s the string
     * @return {@code true} if the string should be capitalized
     */
    private static boolean shouldNotCapitalize(String s) {
        for (int i = 1; i < s.length(); ++i) {
            char c = s.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c == '.' && i < s.length() - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Converts the words in a given string to title case (according to the
     * CSL specification).</p>
     *
     * <p>The implementation of this method is based on JavaScript library
     * {@code to-title-case}, Copyright 2008–2018 David Gouch, released under
     * the MIT license. (<a href="https://github.com/gouch/to-title-case">https://github.com/gouch/to-title-case</a>).
     * It has been slightly modified for the CSL specification.</p>
     *
     * @param str the string to convert
     * @return the converted string
     */
    public static String toTitleCase(String str) {
        if (str == null) {
            return null;
        }

        List<String> l = split(str);
        for (int i = 0; i < l.size(); ++i) {
            String w = l.get(i);
            if (
                    // skip stop words
                    isStopWord(w) &&
                    // skip first and last word
                    i != 0 && i != l.size() - 1 &&
                    // ignore title end and subtitle start
                    !safeEquals(l, i - 3, ":") &&
                    !safeEquals(l, i + 1, ":") &&
                    // ignore stop words that start a hyphenated phrase
                    (!safeEquals(l, i + 1, "-") ||
                            (safeEquals(l, i - 1, "-") && safeEquals(l, i + 1, "-")))
            ) {
                if (!isAllUppercase(w)) {
                    l.set(i, l.get(i).toLowerCase());
                }
                continue;
            }

            // ignore intentional capitalization
            if (shouldNotCapitalize(w)) {
                // nothing to do here
                continue;
            }

            // ignore URLs
            if (safeEquals(l, i + 1, ":") && !safeEquals(l, i + 2, "")) {
                continue;
            }

            // capitalize the first letter
            for (int j = 0; j < w.length(); ++j) {
                char c = w.charAt(j);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                        (c >= '0' && c <= '9') || (c >= '\u00C0' && c <= '\u00FF')) {
                    l.set(i, w.substring(0, j) + Character.toTitleCase(c) + w.substring(j + 1));
                    break;
                }
            }
        }
        return String.join("", l);
    }
}
