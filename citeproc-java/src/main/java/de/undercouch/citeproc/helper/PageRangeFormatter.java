package de.undercouch.citeproc.helper;

import de.undercouch.citeproc.bibtex.PageRange;
import org.apache.commons.lang3.StringUtils;

/**
 * Format page ranges according to CSL rules
 * @author Michel Kraemer
 */
public class PageRangeFormatter {
    public static final String DEFAULT_DELIMITER = "\u2013";

    public enum Format {
        CHICAGO15,
        CHICAGO16,
        EXPANDED,
        MINIMAL,
        MINIMAL2
    }

    public static String format(PageRange range, Format format) {
        return format(range, format, DEFAULT_DELIMITER);
    }

    public static String format(PageRange range, Format format, String delimiter) {
        String first = range.getPageFirst();
        String last = range.getPageLast();
        if (first == null || last == null || first.equals(last)) {
            return range.getLiteral().replace("-", delimiter);
        }

        if (last.length() <= first.length()) {
            if (last.length() < first.length()) {
                last = expanded(first, last);
            }
            switch (format) {
                case CHICAGO15:
                    last = chicago15(first, last);
                    break;

                case CHICAGO16:
                    last = chicago16(first, last);
                    break;

                case EXPANDED:
                    // already done above
                    break;

                case MINIMAL:
                    last = minimal(first, last);
                    break;

                case MINIMAL2:
                    last = minimal2(first, last);
                    break;
            }
        }

        return first + delimiter + last;
    }

    private static String expanded(String first, String last) {
        String fp = getAlphabeticPrefix(first);
        String lp = getAlphabeticPrefix(last);
        if (fp != null && lp == null || fp == null && lp != null) {
            return last;
        }
        if (fp != null) {
            if (fp.equals(lp)) {
                last = last.substring(lp.length());
            } else {
                return last;
            }
        }
        return first.substring(0, first.length() - last.length()) + last;
    }

    private static String getAlphabeticPrefix(String str) {
        int i = 0;
        while (i < str.length() && Character.isAlphabetic(str.charAt(i))) ++i;
        if (i == 0 || i == str.length()) {
            return null;
        }
        return str.substring(0, i);
    }

    private static String minimal(String first, String last) {
        int i = 0;
        while (i < first.length() && first.charAt(i) == last.charAt(i)) {
            ++i;
        }
        return last.substring(i);
    }

    private static String minimal2(String first, String last) {
        int i = 0;
        while (i < first.length() - 2 && first.charAt(i) == last.charAt(i)) {
            ++i;
        }
        return StringUtils.stripStart(last.substring(i), "0");
    }

    private static String chicago15(String first, String last) {
        String fp = getAlphabeticPrefix(first);
        String lp = getAlphabeticPrefix(last);
        if (fp != null && lp == null || fp == null && lp != null) {
            return last;
        }
        if (fp != null) {
            if (fp.equals(lp)) {
                first = first.substring(fp.length());
                last = last.substring(lp.length());
            } else {
                return last;
            }
        }

        if (lp == null) {
            lp = "";
        }

        int nFirst;
        int nLast;
        try {
            nFirst = Integer.parseInt(first);
            nLast = Integer.parseInt(last);
        } catch (NumberFormatException e) {
            return lp + last;
        }

        // noinspection StatementWithEmptyBody
        if (nFirst > 1000 && nFirst <= 9999 && nFirst / 100 != nLast / 100) {
            // use all digits
        } else if (nFirst > 100 && nFirst % 100 > 0) {
            last = minimal2(first, last);
        }
        return lp + last;
    }

    private static String chicago16(String first, String last) {
        String fp = getAlphabeticPrefix(first);
        String lp = getAlphabeticPrefix(last);
        if (fp != null && lp == null || fp == null && lp != null) {
            return last;
        }
        if (fp != null) {
            if (fp.equals(lp)) {
                first = first.substring(fp.length());
                last = last.substring(lp.length());
            } else {
                return last;
            }
        }

        if (lp == null) {
            lp = "";
        }

        int nFirst;
        try {
            nFirst = Integer.parseInt(first);
            Integer.parseInt(last);
        } catch (NumberFormatException e) {
            return lp + last;
        }

        if (nFirst > 100 && nFirst % 100 > 0) {
            last = StringUtils.stripStart(minimal2(first, last), "0");
        }
        return lp + last;
    }
}
