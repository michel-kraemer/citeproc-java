package de.undercouch.citeproc.helper;

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
}
