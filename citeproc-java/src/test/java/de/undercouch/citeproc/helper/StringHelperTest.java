package de.undercouch.citeproc.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test {@link StringHelper}
 * @author Michel Kraemer
 */
public class StringHelperTest {
    /**
     * Sanitize some simple strings
     */
    @Test
    public void sanitize() {
        assertEquals("Kramer", StringHelper.sanitize("Kr\u00E4mer"));
        assertEquals("Giessen", StringHelper.sanitize("Gie\u00dfen"));
        assertEquals("Elsyee", StringHelper.sanitize("Elsy\u00e9e"));
        assertEquals("Champs_Elysees", StringHelper.sanitize("Champs-\u00c9lys\u00e9es"));
        assertEquals("A_test_with_spaces", StringHelper.sanitize("A test with spaces"));
        assertEquals("any_thing_else", StringHelper.sanitize("any+thing*else"));
        assertEquals("Numbers_0124", StringHelper.sanitize("Numbers 0124"));
    }

    /**
     * Tests {@link StringHelper#escapeJava(String)}
     */
    @Test
    public void escapeJava() {
        assertNull(StringHelper.escapeJava(null));
        assertEscapeJava("", "");
        assertEscapeJava("test", "test");
        assertEscapeJava("\\t", "\t");
        assertEscapeJava("\\\\", "\\");
        assertEscapeJava("'", "'");
        assertEscapeJava("\\\"", "\"");
        assertEscapeJava("/", "/");
        assertEscapeJava("\\\\\\b\\r", "\\\b\r");
        assertEscapeJava("\\u4711", "\u4711");
        assertEscapeJava("\\u0815", "\u0815");
        assertEscapeJava("\\u0080", "\u0080");
        assertEscapeJava("\u007f", "\u007f");
        assertEscapeJava(" ", "\u0020");
        assertEscapeJava("\\u0000", "\u0000");
        assertEscapeJava("\\u001F", "\u001f");
    }

    private void assertEscapeJava(String expected, String original) {
        String r = StringHelper.escapeJava(original);
        assertEquals(expected, r);
    }

    /**
     * Tests {@link StringHelper#overlap(CharSequence, CharSequence)}
     */
    @Test
    public void overlap() {
        assertEquals(2, StringHelper.overlap("abcd", "cdef"));
        assertEquals(0, StringHelper.overlap("abcd", "xyz"));
        assertEquals(1, StringHelper.overlap("a", "a"));
        assertEquals(1, StringHelper.overlap("ab", "b"));
        assertEquals(2, StringHelper.overlap("abab", "ab"));
        assertEquals(4, StringHelper.overlap("ababab", "abab"));
        assertEquals(4, StringHelper.overlap("cdabab", "abab"));
        assertEquals(0, StringHelper.overlap("ababcd", "abab"));
        assertEquals(4, StringHelper.overlap("abab", "abab"));
        assertEquals(3, StringHelper.overlap("abcd", "bcdefg"));
        assertEquals(3, StringHelper.overlap("aaaaa", "aaa"));
        assertEquals(0, StringHelper.overlap("aaaaab", "aaa"));
        assertEquals(0, StringHelper.overlap("", "a"));
        assertEquals(0, StringHelper.overlap("a", ""));
        assertEquals(0, StringHelper.overlap("", ""));
        assertEquals(0, StringHelper.overlap("a", null));
        assertEquals(0, StringHelper.overlap(null, "a"));
        assertEquals(0, StringHelper.overlap(null, null));
    }

    /**
     * <p>Test {@link StringHelper#toTitleCase(String)}</p>
     *
     * <p>The test cases here have been copied from the JavaScript library
     * {@code to-title-case}, Copyright 2008–2018 David Gouch, released under
     * the MIT license. (<a href="https://github.com/gouch/to-title-case">https://github.com/gouch/to-title-case</a>).</p>
     */
    @Test
    public void toTitleCase() {
        // general
        assertEquals("One Two", StringHelper.toTitleCase("one two"));
        assertEquals("One Two Three", StringHelper.toTitleCase("one two three"));
        assertEquals("Start a an and as at but by down for from in into nor of on onto or over so the till to up via with yet End",
                StringHelper.toTitleCase("Start a an and as at but by down for from in into nor of on onto or over so the till to up via with yet end"));
        assertEquals("A Small Word Starts", StringHelper.toTitleCase("a small word starts"));
        assertEquals("Small Word Ends On", StringHelper.toTitleCase("small word ends on"));
        assertEquals("Questions?", StringHelper.toTitleCase("questions?"));
        assertEquals("Two Questions?", StringHelper.toTitleCase("Two questions?"));
        assertEquals("One Sentence. Two Sentences.",
                StringHelper.toTitleCase("one sentence. two sentences."));
        assertEquals("We Keep NASA Capitalized",
                StringHelper.toTitleCase("we keep NASA capitalized"));
        assertEquals("Pass camelCase Through",
                StringHelper.toTitleCase("pass camelCase through"));

        // hyphens
        assertEquals("This Sub-Phrase Is Nice",
                StringHelper.toTitleCase("this sub-phrase is nice"));
        assertEquals("Follow Step-by-Step Instructions",
                StringHelper.toTitleCase("follow step-by-step instructions"));
        assertEquals("Easy as One-Two-Three End",
                StringHelper.toTitleCase("easy as one-two-three end"));
        assertEquals("Start On-Demand End",
                StringHelper.toTitleCase("start on-demand end"));
        assertEquals("Start In-or-out End",
                StringHelper.toTitleCase("start in-or-out end"));
        assertEquals("Start E-Commerce End",
                StringHelper.toTitleCase("start e-commerce end"));
        assertEquals("Start E-Mail End",
                StringHelper.toTitleCase("start e-mail end"));

        // punctuation
        assertEquals("Your Hair[cut] Looks (Nice)",
                StringHelper.toTitleCase("your hair[cut] looks (nice)"));
        assertEquals("Keep That Colo(u)r",
                StringHelper.toTitleCase("keep that colo(u)r"));
        assertEquals("Leave Q&A Unscathed",
                StringHelper.toTitleCase("leave Q&A unscathed"));
        assertEquals("Pi\u00F1a Colada While You Listen to \u00C6nima",
                StringHelper.toTitleCase("pi\u00F1a colada while you listen to \u00E6nima"));
        assertEquals("Start Title \u2013 End Title",
                StringHelper.toTitleCase("start title \u2013 end title"));
        assertEquals("Start Title\u2013End Title",
                StringHelper.toTitleCase("start title\u2013end title"));
        assertEquals("Start Title \u2014 End Title",
                StringHelper.toTitleCase("start title \u2014 end title"));
        assertEquals("Start Title\u2014End Title",
                StringHelper.toTitleCase("start title\u2014end title"));
        assertEquals("Start Title - End Title",
                StringHelper.toTitleCase("start title - end title"));

        // quotes
        assertEquals("Don't Break", StringHelper.toTitleCase("don't break"));
        assertEquals("Don\u2019t Break", StringHelper.toTitleCase("don\u2019t break"));
        assertEquals("\"Double Quotes\"",
                StringHelper.toTitleCase("\"double quotes\""));
        assertEquals("Double Quotes \"Inner\" Word",
                StringHelper.toTitleCase("double quotes \"inner\" word"));
        assertEquals("Fancy Double Quotes \u201CInner\u201D Word",
                StringHelper.toTitleCase("fancy double quotes \u201Cinner\u201D word"));
        assertEquals("'Single Quotes'",
                StringHelper.toTitleCase("'single quotes'"));
        assertEquals("Single Quotes 'Inner' Word",
                StringHelper.toTitleCase("single quotes 'inner' word"));
        assertEquals("Fancy Single Quotes \u2018Inner\u2019 Word",
                StringHelper.toTitleCase("fancy single quotes \u2018inner\u2019 word"));
        assertEquals("Single Quotes 'To Stop' Word",
                StringHelper.toTitleCase("single quotes 'To stop' word"));
        assertEquals("Fancy Single Quotes 'To Stop' Word",
                StringHelper.toTitleCase("fancy single quotes 'To stop' word"));
        assertEquals("\u201C\u2018A Twice Quoted Subtitle\u2019\u201D",
                StringHelper.toTitleCase("\u201C\u2018a twice quoted subtitle\u2019\u201D"));
        assertEquals("Have You Read \u201CThe Lottery\u201D?",
                StringHelper.toTitleCase("have you read \u201CThe Lottery\u201D?"));

        // subtitles
        assertEquals("One: Two", StringHelper.toTitleCase("one: two"));
        assertEquals("One Two: Three Four",
                StringHelper.toTitleCase("one two: three four"));
        assertEquals("One Two: \"Three Four\"",
                StringHelper.toTitleCase("one two: \"Three Four\""));
        assertEquals("One On: An End",
                StringHelper.toTitleCase("one on: an end"));
        assertEquals("One On: \"An End\"",
                StringHelper.toTitleCase("one on: \"an end\""));

        // technical
        assertEquals("Email email@example.com Address",
                StringHelper.toTitleCase("email email@example.com address"));
        assertEquals("Email EMail@examPLe.com Address",
                StringHelper.toTitleCase("email EMail@examPLe.com address"));
        assertEquals("You Have an https://example.com/ Title",
                StringHelper.toTitleCase("you have an https://example.com/ title"));
        assertEquals("_Underscores around Words_",
                StringHelper.toTitleCase("_underscores around words_"));
        assertEquals("*Asterisks around Words*",
                StringHelper.toTitleCase("*asterisks around words*"));

        // word combinations
        assertEquals("I Am far from Home",
                StringHelper.toTitleCase("i am far frOm home"));
        assertEquals("Godzilla vs. King Kong",
                StringHelper.toTitleCase("godzilla vs. king kong"));
        assertEquals("My Name Is d'Artagnan",
                StringHelper.toTitleCase("my name is D'artagnan"));

        // non-breaking whitespace
        assertEquals("One\u00a0Two", StringHelper.toTitleCase("one\u00a0two"));

        // miscellaneous
        assertNull(StringHelper.toTitleCase(null));
        assertEquals("", StringHelper.toTitleCase(""));
        assertEquals("Scott Moritz and TheStreet.com\u2019s Million iPhone La-La Land",
                StringHelper.toTitleCase("Scott Moritz and TheStreet.com\u2019s million iPhone la-la land"));
        assertEquals("Back to the 50's", StringHelper.toTitleCase("back to the 50's"));
        assertEquals("Notes and Observations Regarding Apple\u2019s Announcements from \u2018The Beat Goes On\u2019 Special Event",
                StringHelper.toTitleCase("Notes and observations regarding Apple\u2019s announcements from \u2018The Beat Goes On\u2019 special event"));
        assertEquals("2018", StringHelper.toTitleCase("2018"));

        // two sentences with stop word
        assertEquals("This Is the First Sentence. And This Is Another.",
                StringHelper.toTitleCase("this is the first sentence. and this is another."));

        // many whitespaces
        assertEquals("  This   Is      a Sentence  with    Many  Whitespaces:   And   a  Stop Word",
                StringHelper.toTitleCase("  this   is      a sentence  with    many  whitespaces:   and   a  stop word"));

        // all caps
        assertEquals("All Caps Sentence with Stop Word",
                StringHelper.toTitleCase("ALL CAPS SENTENCE WITH STOP WORD"));
    }

    /**
     * Tests {@link StringHelper#initializeName(String, String)}
     */
    @Test
    public void initializeName() {
        // with space
        assertEquals("J L", StringHelper.initializeName("Jean Luc", " "));
        assertEquals("J-L", StringHelper.initializeName("Jean-Luc", " "));
        assertEquals("J-L", StringHelper.initializeName("J.-L.", " "));
        assertEquals("J L", StringHelper.initializeName("J.L.", " "));
        assertEquals("J L", StringHelper.initializeName("J. L.", " "));
        assertEquals("J-L", StringHelper.initializeName("J-Luc", " "));
        assertEquals("J-L", StringHelper.initializeName("Jean-L", " "));
        assertEquals("J L", StringHelper.initializeName("J Luc", " "));
        assertEquals("J L", StringHelper.initializeName("Jean L", " "));
        assertEquals("Je L", StringHelper.initializeName("Je. Luc", " "));
        assertEquals("Cpt J-L", StringHelper.initializeName("Cpt. Jean-Luc", " "));
        assertEquals("J", StringHelper.initializeName("JL", " "));
        assertEquals("J T", StringHelper.initializeName("James T.", " "));
        assertEquals("J T", StringHelper.initializeName("James T", " "));

        // with empty string
        assertEquals("JL", StringHelper.initializeName("Jean Luc", ""));
        assertEquals("J-L", StringHelper.initializeName("Jean-Luc", ""));
        assertEquals("J-L", StringHelper.initializeName("J.-L.", ""));
        assertEquals("JL", StringHelper.initializeName("J.L.", ""));
        assertEquals("JL", StringHelper.initializeName("J. L.", ""));
        assertEquals("J-L", StringHelper.initializeName("J-Luc", ""));
        assertEquals("J-L", StringHelper.initializeName("Jean-L", ""));
        assertEquals("JL", StringHelper.initializeName("J Luc", ""));
        assertEquals("JL", StringHelper.initializeName("Jean L", ""));
        assertEquals("JeL", StringHelper.initializeName("Je. Luc", ""));
        assertEquals("CptJ-L", StringHelper.initializeName("Cpt. Jean-Luc", ""));
        assertEquals("J", StringHelper.initializeName("JL", ""));
        assertEquals("JT", StringHelper.initializeName("James T.", ""));
        assertEquals("JT", StringHelper.initializeName("James T", ""));

        // with period
        assertEquals("J.L.", StringHelper.initializeName("Jean Luc", "."));
        assertEquals("J.-L.", StringHelper.initializeName("Jean-Luc", "."));
        assertEquals("J.-L.", StringHelper.initializeName("J.-L.", "."));
        assertEquals("J.L.", StringHelper.initializeName("J.L.", "."));
        assertEquals("J.L.", StringHelper.initializeName("J. L.", "."));
        assertEquals("J.-L.", StringHelper.initializeName("J-Luc", "."));
        assertEquals("J.-L.", StringHelper.initializeName("Jean-L", "."));
        assertEquals("J.L.", StringHelper.initializeName("J Luc", "."));
        assertEquals("J.L.", StringHelper.initializeName("Jean L", "."));
        assertEquals("Je.L.", StringHelper.initializeName("Je. Luc", "."));
        assertEquals("Cpt.J.-L.", StringHelper.initializeName("Cpt. Jean-Luc", "."));
        assertEquals("J.", StringHelper.initializeName("JL", "."));
        assertEquals("J.T.", StringHelper.initializeName("James T.", "."));
        assertEquals("J.T.", StringHelper.initializeName("James T", "."));

        // with period and space
        assertEquals("J. L.", StringHelper.initializeName("Jean Luc", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("Jean-Luc", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("J.-L.", ". "));
        assertEquals("J. L.", StringHelper.initializeName("J.L.", ". "));
        assertEquals("J. L.", StringHelper.initializeName("J. L.", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("J-Luc", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("Jean-L", ". "));
        assertEquals("J. L.", StringHelper.initializeName("J Luc", ". "));
        assertEquals("J. L.", StringHelper.initializeName("Jean L", ". "));
        assertEquals("Je. L.", StringHelper.initializeName("Je. Luc", ". "));
        assertEquals("Cpt. J.-L.", StringHelper.initializeName("Cpt. Jean-Luc", ". "));
        assertEquals("J.", StringHelper.initializeName("JL", ". "));
        assertEquals("J. T.", StringHelper.initializeName("James T.", ". "));
        assertEquals("J. T.", StringHelper.initializeName("James T", ". "));

        // strings that need normalization
        assertEquals("J. L.", StringHelper.initializeName(" Jean   Luc ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName(" Jean -   Luc ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("Jean\u2013 Luc ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("Jean\u2013-- Luc ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("J.. -L.", ". "));
        assertEquals("J. L.", StringHelper.initializeName("J..L.", ". "));
        assertEquals("J. L.", StringHelper.initializeName("J . L. ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("J . .- L. ", ". "));
        assertEquals("J.-L.", StringHelper.initializeName("J ..- L. ", ". "));
    }

    /**
     * Tests {@link StringHelper#initializeName(String, String, boolean)}
     */
    @Test
    public void initializeNameOnlyNormalize() {
        // with space
        assertEquals("Jean Luc", StringHelper.initializeName("Jean Luc", " ", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean-Luc", " ", true));
        assertEquals("J-L", StringHelper.initializeName("J.-L.", " ", true));
        assertEquals("J L", StringHelper.initializeName("J.L.", " ", true));
        assertEquals("J L", StringHelper.initializeName("J. L.", " ", true));
        assertEquals("J-Luc", StringHelper.initializeName("J-Luc", " ", true));
        assertEquals("Jean-L", StringHelper.initializeName("Jean-L", " ", true));
        assertEquals("J Luc", StringHelper.initializeName("J Luc", " ", true));
        assertEquals("Jean L", StringHelper.initializeName("Jean L", " ", true));
        assertEquals("Je Luc", StringHelper.initializeName("Je. Luc", " ", true));
        assertEquals("Cpt Jean-Luc", StringHelper.initializeName("Cpt. Jean-Luc", " ", true));
        assertEquals("JL", StringHelper.initializeName("JL", " ", true));
        assertEquals("James T", StringHelper.initializeName("James T.", " ", true));
        assertEquals("James T", StringHelper.initializeName("James T", " ", true));

        // with empty string
        assertEquals("Jean Luc", StringHelper.initializeName("Jean Luc", "", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean-Luc", "", true));
        assertEquals("J-L", StringHelper.initializeName("J.-L.", "", true));
        assertEquals("JL", StringHelper.initializeName("J.L.", "", true));
        assertEquals("JL", StringHelper.initializeName("J. L.", "", true));
        assertEquals("J-Luc", StringHelper.initializeName("J-Luc", "", true));
        assertEquals("Jean-L", StringHelper.initializeName("Jean-L", "", true));
        assertEquals("J Luc", StringHelper.initializeName("J Luc", "", true));
        assertEquals("Jean L", StringHelper.initializeName("Jean L", "", true));
        assertEquals("Je Luc", StringHelper.initializeName("Je. Luc", "", true));
        assertEquals("Cpt Jean-Luc", StringHelper.initializeName("Cpt. Jean-Luc", "", true));
        assertEquals("JL", StringHelper.initializeName("JL", "", true));
        assertEquals("James T", StringHelper.initializeName("James T.", "", true));
        assertEquals("James T", StringHelper.initializeName("James T", "", true));

        // with period
        assertEquals("Jean Luc", StringHelper.initializeName("Jean Luc", ".", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean-Luc", ".", true));
        assertEquals("J.-L.", StringHelper.initializeName("J.-L.", ".", true));
        assertEquals("J.L.", StringHelper.initializeName("J.L.", ".", true));
        assertEquals("J.L.", StringHelper.initializeName("J. L.", ".", true));
        assertEquals("J.-Luc", StringHelper.initializeName("J-Luc", ".", true));
        assertEquals("Jean-L.", StringHelper.initializeName("Jean-L", ".", true));
        assertEquals("J. Luc", StringHelper.initializeName("J Luc", ".", true));
        assertEquals("Jean L.", StringHelper.initializeName("Jean L", ".", true));
        assertEquals("Je. Luc", StringHelper.initializeName("Je. Luc", ".", true));
        assertEquals("Cpt. Jean-Luc", StringHelper.initializeName("Cpt. Jean-Luc", ".", true));
        assertEquals("JL", StringHelper.initializeName("JL", ".", true));
        assertEquals("James T.", StringHelper.initializeName("James T.", ".", true));
        assertEquals("James T.", StringHelper.initializeName("James T", ".", true));

        // with period and space
        assertEquals("Jean Luc", StringHelper.initializeName("Jean Luc", ". ", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean-Luc", ". ", true));
        assertEquals("J.-L.", StringHelper.initializeName("J.-L.", ". ", true));
        assertEquals("J. L.", StringHelper.initializeName("J.L.", ". ", true));
        assertEquals("J. L.", StringHelper.initializeName("J. L.", ". ", true));
        assertEquals("J.-Luc", StringHelper.initializeName("J-Luc", ". ", true));
        assertEquals("Jean-L.", StringHelper.initializeName("Jean-L", ". ", true));
        assertEquals("J. Luc", StringHelper.initializeName("J Luc", ". ", true));
        assertEquals("Jean L.", StringHelper.initializeName("Jean L", ". ", true));
        assertEquals("Je. Luc", StringHelper.initializeName("Je. Luc", ". ", true));
        assertEquals("Cpt. Jean-Luc", StringHelper.initializeName("Cpt. Jean-Luc", ". ", true));
        assertEquals("JL", StringHelper.initializeName("JL", ". ", true));
        assertEquals("James T.", StringHelper.initializeName("James T.", ". ", true));
        assertEquals("James T.", StringHelper.initializeName("James T", ". ", true));

        // strings that need normalization
        assertEquals("Jean Luc", StringHelper.initializeName(" Jean   Luc ", ". ", true));
        assertEquals("Jean-Luc", StringHelper.initializeName(" Jean -   Luc ", ". ", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean\u2013 Luc ", ". ", true));
        assertEquals("Jean-Luc", StringHelper.initializeName("Jean\u2013-- Luc ", ". ", true));
        assertEquals("J.-L.", StringHelper.initializeName("J.. -L.", ". ", true));
        assertEquals("J. L.", StringHelper.initializeName("J..L.", ". ", true));
        assertEquals("J. L.", StringHelper.initializeName("J . L. ", ". ", true));
        assertEquals("J.-L.", StringHelper.initializeName("J . .- L. ", ". ", true));
        assertEquals("J.-L.", StringHelper.initializeName("J ..- L. ", ". ", true));
    }
}
