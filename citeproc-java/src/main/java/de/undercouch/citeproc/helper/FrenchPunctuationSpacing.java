package de.undercouch.citeproc.helper;

import java.util.regex.Pattern;

/**
 * <p>Applies rules of French punctuation spacing.</p>
 * <p>See the following links for reference:</p>
 * <ul>
 *     <li><a href="http://forums.zotero.org/discussion/4933/localized-quotes/#Comment_88384">http://forums.zotero.org/discussion/4933/localized-quotes/#Comment_88384</a></li>
 *     <li><a href="https://github.com/mundschenk-at/wp-typography/issues/36">https://github.com/mundschenk-at/wp-typography/issues/36</a></li>
 * </ul>
 * <p>Note: We are following the rules described in the second link above.
 * The sources collected are more convincing than the implementation in
 * citeproc-js.</p>
 * @author Michel Kraemer
 */
public class FrenchPunctuationSpacing {
    // narrow non-breaking spaces before and after guillemets
    private static final Pattern SPACE_AFTER_LEFT_GUILLEMET =
            Pattern.compile("([«|‹])\\h*(?=\\H)");
    private static final Pattern SPACE_BEFORE_RIGHT_GUILLEMET =
            Pattern.compile("(?<=\\H)\\h*([»|›])");

    // equal-width spaces around colon
    private static final Pattern SPACES_AROUND_COLON =
            Pattern.compile("(?<=\\H)\\h*(:[\\s\u00a0])(?=\\H)");

    // narrow non-breaking space before ';', '?', and '!'
    private static final Pattern SPACE_BEFORE_PUNCTUATION =
            Pattern.compile("(?<=[^\\h;?!])\\h*([;?!])");

    /**
     * Apply rules of French punctuation spacing
     * @param str the input string
     * @return the processed string
     */
    public static String apply(String str) {
        str = SPACE_AFTER_LEFT_GUILLEMET.matcher(str).replaceAll("$1\u202F");
        str = SPACE_BEFORE_RIGHT_GUILLEMET.matcher(str).replaceAll("\u202F$1");
        str = SPACES_AROUND_COLON.matcher(str).replaceAll("\u00a0$1");
        str = SPACE_BEFORE_PUNCTUATION.matcher(str).replaceAll("\u202F$1");
        return str;
    }
}
