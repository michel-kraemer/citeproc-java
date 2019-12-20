package de.undercouch.citeproc.helper;

import java.util.regex.Pattern;

/**
 * <p>Replaces straight quotation marks and apostrophes by their typographically
 * correct counterparts.</p>
 * <p>The code is based on smartquotes.js (<a href="https://smartquotes.js.org/">https://smartquotes.js.org/</a>)
 * written by Kelly Martin released under the MIT license. It has been
 * translated to Java and extended to support multiple languages.</p>
 * @author Michel Kraemer
 */
public class SmartQuotes {
    private final String[][] replacements;

    /**
     * Creates the smart-quotes parser
     */
    public SmartQuotes() {
        this.replacements = new String[][] {
                // triple prime
                new String[] { "'''", "\u2034" },
                // beginning "
                new String[] { "(\\W|^)\"(\u2018|'|\\w)", "$1\u201c$2" },
                // ending "
                new String[] { "(\u201c[^\"]*)\"([^\"]*$|[^\u201c\"]*\u201c)", "$1\u201d$2" },
                // remaining " at end of word
                new String[] { "([^0-9])\"", "$1\u201d" },
                // double prime as two single quotes
                new String[] { "''", "\u2033" },
                // beginning '
                new String[] { "(\\W|^)'(\\S)", "$1\u2018$2" },
                // conjunction's possession
                new String[] { "([a-z])'([a-z])", "$1\u2019$2" },
                // abbrev. years like '93
                new String[] { "(\u2018)([0-9]{2}[^\u2019]*)(\u2018([^0-9]|$)|$|\u2019[a-z])", "\u2019$2$3" },
                // ending '
                new String[] { "((\u2018[^']*)|[a-z])'([^0-9]|$)", "$1\u2019$3" },
                // backwards apostrophe
                new String[] { "(\\B|^)\u2018(?=([^\u2018\u2019]*\u2019\\b)*([^\u2018\u2019]*\\B\\W[\u2018\u2019]\\b|[^\u2018\u2019]*$))", "$1\u2019" },
                // double prime
                new String[] { "\"", "\u2033" },
                // prime
                new String[] { "'", "\u2032" }
        };
    }

    /**
     * Replace straight quotation marks and apostrophes in the given string
     * by their typographically correct counterparts.
     * @param str the input string
     * @return the processed string
     */
    public String apply(String str) {
        for (String[] replacement : replacements) {
            Pattern p = Pattern.compile(replacement[0], Pattern.CASE_INSENSITIVE);
            str = p.matcher(str).replaceAll(replacement[1]);
        }
        return str;
    }
}
