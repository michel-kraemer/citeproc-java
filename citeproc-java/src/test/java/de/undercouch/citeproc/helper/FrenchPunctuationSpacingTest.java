package de.undercouch.citeproc.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FrenchPunctuationSpacing}
 * @author MicheL Kraemer
 */
public class FrenchPunctuationSpacingTest {
    /**
     * Test the {@link FrenchPunctuationSpacing#apply(String)} method
     */
    @Test
    public void apply() {
        assertEquals("His «\u202FAnonymous\u202F» Life",
                FrenchPunctuationSpacing.apply("His « Anonymous » Life"));
        assertEquals("«\u202FAnonymous\u202F»",
                FrenchPunctuationSpacing.apply("« Anonymous »"));
        assertEquals("«\u202F»",
                FrenchPunctuationSpacing.apply("«»"));
        assertEquals("His «",
                FrenchPunctuationSpacing.apply("His «"));
        assertEquals("His «  ",
                FrenchPunctuationSpacing.apply("His «  "));
        assertEquals("«",
                FrenchPunctuationSpacing.apply("«"));
        assertEquals("» Life",
                FrenchPunctuationSpacing.apply("» Life"));
        assertEquals("  » Life",
                FrenchPunctuationSpacing.apply("  » Life"));
        assertEquals("»",
                FrenchPunctuationSpacing.apply("»"));
        assertEquals("He said\u00a0: «\u202FThis is my ‹\u202Fanonymous life\u202F›\u202F»",
                FrenchPunctuationSpacing.apply("He said: «This is my ‹anonymous life›»"));
        assertEquals("He said: ",
                FrenchPunctuationSpacing.apply("He said: "));
        assertEquals(": foobar",
                FrenchPunctuationSpacing.apply(": foobar"));
        assertEquals("XIV:XI",
                FrenchPunctuationSpacing.apply("XIV:XI"));
        assertEquals("Is this my anonymous life\u202F?",
                FrenchPunctuationSpacing.apply("Is this my anonymous life?"));
        assertEquals("Is this really my anonymous life\u202F??",
                FrenchPunctuationSpacing.apply("Is this really my anonymous life??"));
        assertEquals("This is my anonymous life\u202F?!",
                FrenchPunctuationSpacing.apply("This is my anonymous life?!"));
        assertEquals("This is my anonymous life\u202F!",
                FrenchPunctuationSpacing.apply("This is my anonymous life!"));
        assertEquals("Yes, this is my anonymous life\u202F!!",
                FrenchPunctuationSpacing.apply("Yes, this is my anonymous life!!"));
        assertEquals("This is my anonymous life\u202F; I like it\u202F!",
                FrenchPunctuationSpacing.apply("This is my anonymous life; I like it!"));
        assertEquals("; I like it\u202F!",
                FrenchPunctuationSpacing.apply("; I like it!"));
        assertEquals("  !?",
                FrenchPunctuationSpacing.apply("  !?"));
        assertEquals("Is this my anonymous life\u202F? Yes, it is\u202F!",
                FrenchPunctuationSpacing.apply("Is this my anonymous life? Yes, it is!"));
        assertEquals("Did he say\u00a0: «\u202FThis is my ‹\u202Fanonymous life\u202F›\u202F»\u202F?",
                FrenchPunctuationSpacing.apply("Did he say: «This is my ‹anonymous life›»?"));
    }
}
