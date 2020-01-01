package de.undercouch.citeproc.helper;

import java.util.Locale;
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
    private static final String NUMBER = "\\p{N}";
    private static final String NO_NUMBER = "[^" + NUMBER + "]";
    private static final String LETTER = "\\p{L}";
    private static final String WORD = "[" + LETTER + "_" + NUMBER + "]";
    private static final String NO_WORD = "[^" + LETTER + "_" + NUMBER + "]";

    private static final String LEFT_SINGLE_QUOTE = "\u2018";
    private static final String RIGHT_SINGLE_QUOTE = "\u2019";
    private static final String LEFT_DOUBLE_QUOTE = "\u201c";
    private static final String RIGHT_DOUBLE_QUOTE = "\u201d";
    private static final String PRIME = "\u2032";
    private static final String DOUBLE_PRIME = "\u2033";
    private static final String TRIPLE_PRIME = "\u2034";

    private final Pattern[] patterns;
    private final String[] replacements;

    /**
     * Creates the smart-quotes parser
     */
    public SmartQuotes() {
        this(LEFT_SINGLE_QUOTE, RIGHT_SINGLE_QUOTE,
                LEFT_DOUBLE_QUOTE, RIGHT_DOUBLE_QUOTE, Locale.ENGLISH);
    }

    /**
     * Creates the smart-quotes parser with custom symbols
     * @param leftSingleQuote custom left single quotation mark
     * @param rightSingleQuote custom right singe quotation mark
     * @param leftDoubleQuote custom left double quotation mark
     * @param rightDoubleQuote custom right double quotation mark
     * @param locale a locale object used to apply special rules and corner cases
     */
    public SmartQuotes(String leftSingleQuote, String rightSingleQuote,
            String leftDoubleQuote, String rightDoubleQuote, Locale locale) {
        this(leftSingleQuote, rightSingleQuote, leftDoubleQuote, rightDoubleQuote,
                RIGHT_SINGLE_QUOTE, PRIME, DOUBLE_PRIME, TRIPLE_PRIME, locale);
    }

    /**
     * Creates the smart-quotes parser with custom symbols
     * @param leftSingleQuote custom left single quotation mark
     * @param rightSingleQuote custom right singe quotation mark
     * @param leftDoubleQuote custom left double quotation mark
     * @param rightDoubleQuote custom right double quotation mark
     * @param apostrophe custom apostrophe
     * @param prime custom prime
     * @param doublePrime custom double prime
     * @param triplePrime custom triple prime
     * @param locale a locale object used to apply special rules and corner cases
     */
    @SuppressWarnings({"RegExpUnexpectedAnchor", "Annotator"})
    public SmartQuotes(String leftSingleQuote, String rightSingleQuote,
            String leftDoubleQuote, String rightDoubleQuote, String apostrophe,
            String prime, String doublePrime, String triplePrime, Locale locale) {
        String[][] replacements = new String[][] {
                // whitelist (works for English only, bummer)
                new String[] { "'(em|cause|twas|tis|til)([^a-z])", (locale != null && locale.getLanguage().equalsIgnoreCase("en")) ? apostrophe + "$1$2" : "$0" },
                // triple prime
                new String[] { "'''", triplePrime },
                // beginning "
                new String[] { "(" + NO_WORD + "|^)\"(" + leftSingleQuote + "|'|" + WORD + ")", "$1" + leftDoubleQuote + "$2" },
                // ending "
                new String[] { "(" + leftDoubleQuote + "[^\"]*)\"([^\"]*$|[^" + leftDoubleQuote + "\"]*" + leftDoubleQuote + ")", "$1" + rightDoubleQuote + "$2" },
                // remaining " at end of word
                new String[] { "(" + NO_NUMBER + ")\"", "$1" + rightDoubleQuote },
                // double prime as two single quotes
                new String[] { "''", doublePrime },
                // beginning '
                new String[] { "(" + NO_WORD + "|^)'(\\S)", "$1" + leftSingleQuote + "$2" },
                // conjunction's possession
                new String[] { "(" + LETTER + "|" + NUMBER + ")'(" + LETTER + ")", "$1" + apostrophe + "$2" },
                // abbrev. years like '93
                new String[] { "(" + leftSingleQuote + ")([0-9]{2}[^" + rightSingleQuote + apostrophe + "]*)(" + leftSingleQuote + "(" + NO_NUMBER + "|$)|$|" + rightSingleQuote + apostrophe + LETTER + ")", apostrophe + "$2$3" },
                // ending '
                new String[] { "((" + leftSingleQuote + "[^']*)|" + LETTER + ")'(" + NO_NUMBER + "|$)", "$1" + rightSingleQuote + "$3" },
                // backwards apostrophe
                new String[] { "(\\B|^)" + leftSingleQuote + "(?=([^" + leftSingleQuote + rightSingleQuote + apostrophe + "]*[" + rightSingleQuote + apostrophe + "]\\b)*([^" + leftSingleQuote + rightSingleQuote + apostrophe + "]*\\B" + NO_WORD + "[" + leftSingleQuote + rightSingleQuote + apostrophe + "]\\b|[^" + leftSingleQuote + rightSingleQuote + apostrophe + "]*$))", "$1" + rightSingleQuote },
                // double prime (not first character)
                new String[] { "(.)\"", "$1" + doublePrime },
                // left double quote at the beginning of string
                new String[] { "^\"", leftDoubleQuote },
                // prime
                new String[] { "'", prime }
        };

        this.patterns = new Pattern[replacements.length];
        this.replacements = new String[replacements.length];
        for (int i = 0; i < replacements.length; ++i) {
            this.patterns[i] = Pattern.compile(replacements[i][0]);
            this.replacements[i] = replacements[i][1];
        }
    }

    /**
     * Replace straight quotation marks and apostrophes in the given string
     * by their typographically correct counterparts.
     * @param str the input string
     * @return the processed string
     */
    public String apply(String str) {
        for (int i = 0; i < patterns.length; ++i) {
            str = patterns[i].matcher(str).replaceAll(replacements[i]);
        }
        return str;
    }
}
