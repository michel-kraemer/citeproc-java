package de.undercouch.citeproc.helper;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link SmartQuotes}
 * @author MicheL Kraemer
 */
public class SmartQuotesTest {
    /**
     * Test the {@link SmartQuotes#apply(String)} method
     */
    @Test
    public void apply() {
        SmartQuotes sq = new SmartQuotes();

        // tests from smartquotes.js (https://smartquotes.js.org/)
        // written by Kelly Martin released under the MIT license
        assertEquals("“test”", sq.apply("\"test\""));
        assertEquals("the— “test”", sq.apply("the— \"test\""));
        assertEquals("‘test’", sq.apply("'test'"));
        assertEquals("ma’am", sq.apply("ma'am"));
        assertEquals("’em", sq.apply("'em"));
        assertEquals("Marshiness of ’Ammercloth’s",
                sq.apply("Marshiness of 'Ammercloth's"));
        assertEquals("’95", sq.apply("'95"));
        assertEquals("‴", sq.apply("'''"));
        assertEquals("″", sq.apply("''"));
        assertEquals("“Better than a 6′5″ whale.”",
                sq.apply("\"Better than a 6'5\" whale.\""));
        assertEquals("“It’s my ‘#1’ choice!” - 12″ Foam Finger from ’93",
                sq.apply("\"It's my '#1' choice!\" - 12\" Foam Finger from '93"));
        assertEquals("“Say ‘what?’” says a Mill’s Pet Barn employee.",
                sq.apply("\"Say 'what?'\" says a Mill's Pet Barn employee."));
        assertEquals("“Quote?”: Description",
                sq.apply("\"Quote?\": Description"));
        assertEquals("‘Quo Te?’: Description",
                sq.apply("'Quo Te?': Description"));
        assertEquals("“De Poesjes van Kevin?”: Something, something",
                sq.apply("\"De Poesjes van Kevin?\": Something, something"));
        assertEquals("And then she blurted, “I thought you said, ‘I don’t like ’80s music’?”",
                sq.apply("And then she blurted, \"I thought you said, 'I don't like '80s music'?\""));

        // further tests
        assertEquals("That’s and it’s and couldn’t.",
                sq.apply("That's and it's and couldn't."));
        assertEquals("“‘That’s so cool,’ he said.”",
                sq.apply("\"'That's so cool,' he said.\""));
        assertEquals("“‘That’s so “cool”,’ he said.”",
                sq.apply("\"'That's so \"cool\",' he said.\""));

        // tests from https://medium.design/quotation-marks-c8993b54417c
        assertEquals("12½″ record, 5′10⅝″ height",
                sq.apply("12½\" record, 5'10⅝\" height"));
        assertEquals("iPad 3’s battery life",
                sq.apply("iPad 3's battery life"));
        assertEquals("Book ’em, Danno. Rock’n’roll. ’Cause ’twas the season.",
                sq.apply("Book 'em, Danno. Rock'n'roll. 'Cause 'twas the season."));
        assertEquals("This is ‘empathy’.",
                sq.apply("This is 'empathy'."));
        assertEquals("Book ’em",
                sq.apply("Book 'em"));
        assertEquals("’85 was a good year. The entire ’80s were",
                sq.apply("'85 was a good year. The entire '80s were"));
    }

    /**
     * Characters with accents as well as umlauts
     */
    @Test
    public void accented() {
        SmartQuotes sq = new SmartQuotes();

        assertEquals("“Águila”", sq.apply("\"Águila\""));
        assertEquals("“águila”", sq.apply("\"águila\""));
        assertEquals("“Aguila”", sq.apply("\"Aguila\""));
        assertEquals("“aguila”", sq.apply("\"aguila\""));

        assertEquals("“Äquator”", sq.apply("\"Äquator\""));
        assertEquals("“ärgerlich”", sq.apply("\"ärgerlich\""));

        assertEquals("“Hä”", sq.apply("\"Hä\""));
    }

    /**
     * Similar to {@link #apply()} but with German quotation marks
     */
    @Test
    public void german() {
        SmartQuotes sq = new SmartQuotes("‚", "‘", "„", "“", Locale.GERMAN);

        assertEquals("„test“", sq.apply("\"test\""));
        assertEquals("Der— „Test“", sq.apply("Der— \"Test\""));
        assertEquals("‚test‘", sq.apply("'test'"));
        assertEquals("Er kann’s", sq.apply("Er kann's"));
        assertEquals("Marshiness von ‘Ammercloth’s",
                sq.apply("Marshiness von 'Ammercloth's"));
        assertEquals("’95", sq.apply("'95"));
        assertEquals("‴", sq.apply("'''"));
        assertEquals("″", sq.apply("''"));
        assertEquals("„Besser als ein 6′5″ Wal.“",
                sq.apply("\"Besser als ein 6'5\" Wal.\""));
        assertEquals("„Ich hab’s auf ‚#1‘ gesetzt!“ - Der 12″ Schaumstofffinger von ’93",
                sq.apply("\"Ich hab's auf '#1' gesetzt!\" - Der 12\" Schaumstofffinger von '93"));
        assertEquals("„Sag ‚was?‘“ sagt der Mitarbeiter der Mill’s Pet Barn.",
                sq.apply("\"Sag 'was?'\" sagt der Mitarbeiter der Mill's Pet Barn."));
        assertEquals("„Quote?“: Beschreibung",
                sq.apply("\"Quote?\": Beschreibung"));
        assertEquals("‚Quo Te?‘: Beschreibung",
                sq.apply("'Quo Te?': Beschreibung"));
        assertEquals("„De Poesjes van Kevin?“: Irgendwas, irgendwas",
                sq.apply("\"De Poesjes van Kevin?\": Irgendwas, irgendwas"));
        assertEquals("Und dann platze es aus ihr heraus: „Ich dachte, du sagtest: ‚Ich mag keine 80er-Musik‘?“",
                sq.apply("Und dann platze es aus ihr heraus: \"Ich dachte, du sagtest: 'Ich mag keine 80er-Musik'?\""));

        assertEquals("’85 war ein gutes Jahr. Die ganzen 80er waren so.",
                sq.apply("'85 war ein gutes Jahr. Die ganzen 80er waren so."));
    }

    /**
     * Test that opening quotation mark at the beginning of the string is
     * converted correctly, even if it does not follow a letter or a number.
     */
    @Test
    public void openQuoteNoPrime() {
        SmartQuotes sq = new SmartQuotes();
        assertEquals("“Test", sq.apply("\"Test"));
        assertEquals("“-Test", sq.apply("\"-Test"));
        assertEquals("″-Test", sq.apply("″-Test"));
    }
}
