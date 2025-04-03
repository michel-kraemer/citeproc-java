package de.undercouch.citeproc.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * See <a href="https://raw.githubusercontent.com/citation-style-language/schema/master/schemas/styles/stop-words.json">stop-words.json</a>
     */
    private final static String TITLECASE_STOPWORD_FOLLOWEDBY = "[^\\p{L}\\d_:'\"‘’“”]+";
    private final static Pattern[] TITLECASE_STOPWORD_PATTERNS;
    static {
        String p = TITLECASE_STOPWORD_FOLLOWEDBY;
        String[] stopwords = new String[] {
                "^a" + p, "^according\\s+to" + p, "^across" + p, "^afore" + p,
                "^after" + p, "^against" + p, "^ahead\\s+of" + p, "^along" + p,
                "^alongside" + p, "^amid" + p, "^amidst" + p, "^among" + p,
                "^amongst" + p, "^an" + p, "^and" + p, "^anenst" + p,
                "^apart\\s+from" + p, "^apropos" + p, "^apud" + p,
                "^around" + p, "^as" + p, "^as\\s+regards" + p, "^aside" + p,
                "^astride" + p, "^at" + p, "^athwart" + p, "^atop" + p,
                "^back\\s+to" + p, "^barring" + p, "^because\\s+of" + p,
                "^before" + p, "^behind" + p, "^below" + p, "^beneath" + p,
                "^beside" + p, "^besides" + p, "^between" + p, "^beyond" + p,
                "^but" + p, "^by" + p, "^c" + p, "^ca" + p, "^circa" + p,
                "^close\\s+to" + p, "^d['’](?=\\p{L})", "^de" + p, "^despite" + p,
                "^down" + p, "^due\\s+to" + p, "^during" + p, "^et" + p,
                "^except" + p, "^far\\s+from" + p, "^for" + p, "^forenenst" + p,
                "^from" + p, "^given" + p, "^in" + p, "^inside" + p,
                "^instead\\s+of" + p, "^into" + p, "^lest" + p, "^like" + p,
                "^modulo" + p, "^near" + p, "^next" + p, "^nor" + p,
                "^notwithstanding" + p, "^of" + p, "^off" + p, "^on" + p,
                "^onto" + p, "^or" + p, "^out" + p, "^outside\\s+of" + p,
                "^over" + p, "^per" + p, "^plus" + p, "^prior\\s+to" + p,
                "^pro" + p,  "^pursuant\\s+to" + p, "^qua" + p,
                "^rather\\s+than" + p, "^regardless\\s+of" + p, "^sans" + p,
                "^since" + p, "^so" + p, "^such\\s+as" + p, "^than" + p,
                "^that\\s+of" + p, "^the" + p, "^through" + p,
                "^throughout" + p, "^thru" + p, "^thruout" + p, "^till" + p,
                "^to" + p, "^toward" + p, "^towards" + p, "^under" + p,
                "^underneath" + p, "^until" + p, "^unto" + p, "^up" + p,
                "^upon" + p, "^v\\." + p, "^van" + p, "^versus" + p, "^via" + p,
                "^vis-à-vis" + p, "^von" + p, "^vs\\." + p, "^where\\s+as" + p,
                "^with" + p, "^within" + p, "^without" + p, "^yet" + p
        };

        // look for longest matches first
        Arrays.sort(stopwords, Comparator.comparingInt(String::length).reversed());

        // compile to regex
        TITLECASE_STOPWORD_PATTERNS = new Pattern[stopwords.length];
        for (int i = 0; i < stopwords.length; ++i) {
            TITLECASE_STOPWORD_PATTERNS[i] = Pattern.compile(stopwords[i],
                    Pattern.CASE_INSENSITIVE);
        }
    }
    private static final Pattern WORD_PATTERN =
            Pattern.compile("^[\\p{L}\\d][\\p{L}\\d\\[\\]()'’&]*");

    /**
     * Based on Markdown by John Gruber
     * (<a href="https://daringfireball.net/projects/markdown/">https://daringfireball.net/projects/markdown/</a>).
     * Released under a BSD-style license.
     */
    private static final Pattern MAIL_PATTERN =
            Pattern.compile("^(mailto:)?([\\w.-]+@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Based on John Gruber's URL regex
     * (<a href="https://gist.github.com/gruber/8891611">https://gist.github.com/gruber/8891611</a> or
     * <a href="https://daringfireball.net/2010/07/improved_regex_for_matching_urls">https://daringfireball.net/2010/07/improved_regex_for_matching_urls</a>)
     * released under public domain. Slightly simplified.
     */
    private static final String TLDs = "(com|net|org|edu|gov|mil|aero|asia|biz|cat|" +
            "coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|" +
            "ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|" +
            "bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|" +
            "cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|" +
            "es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|" +
            "gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|" +
            "je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|" +
            "lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|" +
            "mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|" +
            "pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|" +
            "si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|" +
            "tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|" +
            "vn|vu|wf|ws|ye|yt|yu|za|zm|zw)";
    private static final Pattern URL_PATTERN =
            Pattern.compile("^((?:https?:(?:/{1,3}|[a-z0-9%])|[a-z0-9.\\-]+[.]" + TLDs + "/)" +
                    "(?:[^\\s()<>{}\\[\\]]+|\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|" +
                    "\\(\\S+?\\))+(?:\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|" +
                    "\\(\\S+?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’])|" +
                    "(?<!@)[a-z0-9]+(?:[.\\-][a-z0-9]+)*[.]" + TLDs + "\\b/?(?!@))",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern POSSESSIVE_S_PATTERN = Pattern.compile(
            "^['’]s" + TITLECASE_STOPWORD_FOLLOWEDBY, Pattern.CASE_INSENSITIVE);

    private static class NamePart {
        final String part;
        final boolean hyphen;
        final boolean alreadyInitialized;

        NamePart(String part, boolean hyphen, boolean alreadyInitialized) {
            this.part = part;
            this.hyphen = hyphen;
            this.alreadyInitialized = alreadyInitialized;
        }
    }

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
                case 'À':
                case 'Á':
                case 'Ã':
                case 'Ä':
                    sb.append('A');
                    break;

                case 'È':
                case 'É':
                case 'Ë':
                    sb.append('E');
                    break;

                case 'Ì':
                case 'Í':
                case 'Ï':
                    sb.append('I');
                    break;

                case 'Ò':
                case 'Ó':
                case 'Õ':
                case 'Ö':
                    sb.append('O');
                    break;

                case 'Ù':
                case 'Ú':
                case 'Ü':
                    sb.append('U');
                    break;

                case 'à':
                case 'á':
                case 'ã':
                case 'ä':
                    sb.append('a');
                    break;

                case 'è':
                case 'é':
                case 'ë':
                    sb.append('e');
                    break;

                case 'ì':
                case 'í':
                case 'ï':
                    sb.append('i');
                    break;

                case 'ò':
                case 'ó':
                case 'ö':
                case 'õ':
                    sb.append('o');
                    break;

                case 'ù':
                case 'ú':
                case 'ü':
                    sb.append('u');
                    break;

                case 'Ñ':
                    sb.append('N');
                    break;

                case 'ñ':
                    sb.append('n');
                    break;

                case 'Č':
                    sb.append('C');
                    break;

                case 'Š':
                    sb.append('S');
                    break;

                case 'Ž':
                    sb.append('Z');
                    break;

                case 'č':
                    sb.append('c');
                    break;

                case 'š':
                    sb.append('s');
                    break;

                case 'ž':
                    sb.append('z');
                    break;

                case 'ß':
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
     * Check if all letters in the given string are uppercase
     * @param s the string
     * @return {@code true} if the letters in the given string are all uppercase
     */
    private static boolean titleAllUppercase(String s) {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isLetter(c) && !Character.isUpperCase(c)) {
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
    private static boolean shouldCapitalize(String s) {
        // do not capitalize single greek characters used as symbols in
        // scientific papers
        if (s.length() == 1 && s.charAt(0) >= 0x0370 && s.charAt(0) <= 0x03FF) {
            return false;
        }

        for (int i = 1; i < s.length(); ++i) {
            if (Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldStopwordLowercase(String w, String str) {
        // exception
        if (w.equalsIgnoreCase("d'") || w.equalsIgnoreCase("d’")) {
            // check next word
            Matcher wm = WORD_PATTERN.matcher(str.substring(2));
            // do not lowercase "d'" if the word immediately following it
            // is also completely uppercase
            return !wm.find() || !titleAllUppercase(str.substring(2, wm.end() + 2));
        }

        // don't lowercase stop words that are all uppercase
        return !titleAllUppercase(w);
    }

    /**
     * Converts the words in a given string to title case (according to the
     * CSL specification)
     * @param str the string to convert
     * @return the converted string
     */
    public static String toTitleCase(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return str;
        }

        // convert all caps title to lowercase
        if (titleAllUppercase(str)) {
            str = str.toLowerCase(Locale.ENGLISH);
        }

        StringBuilder sb = new StringBuilder();
        int nwords = 0;
        int i = 0;
        while (i < str.length()) {
            String ss = str.substring(i);
            int swe = -1;

            // check for stop word
            if (i > 0 && nwords > 0) {
                char prevChar = str.charAt(i - 1);
                for (Pattern p : TITLECASE_STOPWORD_PATTERNS) {
                    Matcher m = p.matcher(ss);
                    if (m.find()) {
                        if (ss.charAt(m.end() - 1) == '-' && prevChar != '-') {
                            // skip stop words followed by a hyphen but not preceded
                            // by a hyphen (e.g. skip "on" in " On-demand" but not "by"
                            // in "Step-by-Step")
                            continue;
                        }
                        swe = m.end();
                        break;
                    }
                }
            }
            if (swe >= 0) {
                String w = ss.substring(0, swe);
                if (shouldStopwordLowercase(w, ss)) {
                    sb.append(w.toLowerCase(Locale.ENGLISH));
                } else {
                    sb.append(w);
                }
                i += swe;
                nwords++;
                continue;
            }

            // check if we found a possessive 's
            if (i > 0 && Character.isLetterOrDigit(str.charAt(i - 1))) {
                Matcher pm = POSSESSIVE_S_PATTERN.matcher(ss);
                if (pm.find()) {
                    sb.append(ss.substring(0, pm.end()).toLowerCase(Locale.ENGLISH));
                    i += pm.end();
                    nwords++;
                    continue;
                }
            }

            // check for mail addresses
            Matcher mam = MAIL_PATTERN.matcher(ss);
            if (mam.find()) {
                sb.append(ss, 0, mam.end());
                i += mam.end();
                nwords++;
                continue;
            }

            // check for urls
            Matcher um = URL_PATTERN.matcher(ss);
            if (um.find()) {
                sb.append(ss, 0, um.end());
                i += um.end();
                nwords++;
                continue;
            }

            // check for normal word
            Matcher wm = WORD_PATTERN.matcher(ss);
            if (wm.find()) {
                String w = ss.substring(0, wm.end());
                if (shouldCapitalize(w)) {
                    w = Character.toTitleCase(w.charAt(0)) + w.substring(1);
                }
                sb.append(w);
                i += wm.end();
                nwords++;
                continue;
            }

            char c = str.charAt(i);
            if (c == ':' || c == '.' || c == '“' || c == '‘') {
                // start a new sentence
                nwords = 0;
            }
            // maybe start a new sentence but only if there are no whitespace
            // characters following the quote
            boolean maybeNewSentence = c == '"' || c == '\'';

            sb.append(c);
            ++i;

            // eat up whitespaces
            boolean foundWhitespace = false;
            while (i < str.length() && Character.isWhitespace(c = str.charAt(i))) {
                sb.append(c);
                foundWhitespace = true;
                ++i;
            }

            if (maybeNewSentence && !foundWhitespace) {
                nwords = 0;
            }
        }
        return sb.toString();
    }

    /**
     * Check if a string contains no other characters than those from the
     * Unicode scripts "Latin", "Common", or "Inherited"
     * @param s the string to check
     * @return if the string contains only latin, common, or inherited
     * characters
     */
    public static boolean containsLatinScriptOnly(String s) {
        for (int i = 0; i < s.length(); ) {
            int codePoint = s.codePointAt(i);
            Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);

            if (script != Character.UnicodeScript.LATIN &&
                script != Character.UnicodeScript.COMMON &&
                script != Character.UnicodeScript.INHERITED) {
                return false;
            }

            i += Character.charCount(codePoint);
        }
        return true;
    }

    /**
     * Parse the given name, split it into parts, and convert them to initials
     * @param name the name to convert
     * @param initializeWith the string to append to each initial
     * @return the converted name
     */
    public static String initializeName(String name, String initializeWith) {
        return initializeName(name, initializeWith, false);
    }

    /**
     * Parse the given name, split it into parts, and either convert them all
     * to initials or only normalize existing initials
     * @param name the name to convert
     * @param initializeWith the string to append to each initial
     * @param onlyNormalize {@code true} if only existing initials should be
     * normalized and uninitialized names should be kept as is
     * @return the converted name
     */
    public static String initializeName(String name, String initializeWith,
            boolean onlyNormalize) {
        if (!containsLatinScriptOnly(name)) {
            // initialization only applies to names with Latin characters
            return name;
        }

        // trim string, normalize spaces, normalize hyphens
        name = name.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*\\.", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("\\s*[-‐‑‒–—―]+\\s*", "-");

        List<NamePart> parts = new ArrayList<>();
        int lp = 0;
        for (int i = 1; i <= name.length(); ++i) {
            if (i == name.length() || name.charAt(i) == ' ') {
                if (i > lp) {
                    String sub = name.substring(lp, i);
                    parts.add(new NamePart(sub, false, sub.length() == 1 && isAllUppercase(sub)));
                }
                lp = i + 1;
            } else if (name.charAt(i) == '-') {
                if (i > lp) {
                    String sub = name.substring(lp, i);
                    parts.add(new NamePart(sub, true, sub.length() == 1 && isAllUppercase(sub)));
                }
                lp = i + 1;
            } else if (name.charAt(i) == '.' && (i < name.length() - 1 && name.charAt(i + 1) == '-')) {
                if (i > lp) {
                    parts.add(new NamePart(name.substring(lp, i), true, true));
                }
                i++;
                lp = i + 1;
            } else if (name.charAt(i) == '.') {
                if (i > lp) {
                    parts.add(new NamePart(name.substring(lp, i), false, true));
                }
                lp = i + 1;
            }
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            NamePart p = parts.get(i);
            if (onlyNormalize && i > 0 &&
                    (!p.alreadyInitialized || !parts.get(i - 1).alreadyInitialized) &&
                    result.length() > 0 &&
                    result.charAt(result.length() - 1) != ' ' &&
                    result.charAt(result.length() - 1) != '-') {
                result.append(" ");
            }
            if (onlyNormalize || p.alreadyInitialized) {
                result.append(p.part);
            } else {
                result.append(p.part.charAt(0));
            }
            if (!onlyNormalize || p.alreadyInitialized) {
                result.append(initializeWith);
            }
            if (p.hyphen) {
                result.append("-");
            }
        }

        return result.toString()
                .replaceAll("\\s+-", "-")
                .trim();
    }
}
