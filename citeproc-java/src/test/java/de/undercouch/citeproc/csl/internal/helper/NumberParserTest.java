package de.undercouch.citeproc.csl.internal.helper;

import de.undercouch.citeproc.csl.CSLLabel;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link NumberParser}
 * @author Michel Kraemer
 */
public class NumberParserTest {
    /**
     * Test if simple numbers can be parsed
     */
    @Test
    public void simple() {
        assertEquals(Collections.singletonList(
                new NumberElement("10")),
                NumberParser.parse("10"));
        assertEquals(Collections.singletonList(
                new NumberElement("10")),
                NumberParser.parse("  10   "));
        assertEquals(Collections.singletonList(
                new NumberElement("10a")),
                NumberParser.parse("10a"));
        assertEquals(Collections.singletonList(
                new NumberElement("foo")),
                NumberParser.parse("foo"));
    }

    /**
     * Test if number ranges can be parsed
     */
    @Test
    public void range() {
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", null, true)),
                NumberParser.parse("10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10a, I; 10c, I", null, true)),
                NumberParser.parse("10a,I; 10c,I"));
        assertEquals(Collections.singletonList(
                new NumberElement("1, 2, 3", null, true)),
                NumberParser.parse("1,2,3"));
    }

    /**
     * Test if single numbers with labels can be parsed
     */
    @Test
    public void label() {
        assertEquals(Collections.singletonList(
                new NumberElement("4", CSLLabel.PAGE, false)),
                NumberParser.parse("p. 4"));
        assertEquals(Collections.singletonList(
                new NumberElement("10", CSLLabel.CHAPTER, false)),
                NumberParser.parse("ch. 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("ch 10")),
                NumberParser.parse("ch 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("cha 10")),
                NumberParser.parse("cha 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("10", CSLLabel.CHAPTER, false)),
                NumberParser.parse("chap. 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("10", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("1.5", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 1.5"));
        assertEquals(Collections.singletonList(
                new NumberElement("10", CSLLabel.CHAPTER, false)),
                NumberParser.parse("chapter 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("10", CSLLabel.SECTION, false)),
                NumberParser.parse("section 10"));
        assertEquals(Collections.singletonList(
                new NumberElement("10a-b", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 10a-b"));
        assertEquals(Collections.singletonList(
                new NumberElement("10a:b", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 10a:b"));
        assertEquals(Collections.singletonList(
                new NumberElement("1.a", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 1.a"));
        assertEquals(Collections.singletonList(
                new NumberElement("I.a", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. I.a"));
        assertEquals(Collections.singletonList(
                new NumberElement("1.I", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. 1.I"));
        assertEquals(Collections.singletonList(
                new NumberElement("I", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. I"));
        assertEquals(Collections.singletonList(
                new NumberElement("IV", CSLLabel.SECTION, false)),
                NumberParser.parse("sec. IV"));
        assertEquals(Collections.singletonList(
                new NumberElement("foo. 4")),
                NumberParser.parse("foo. 4"));
        assertEquals(Collections.singletonList(
                new NumberElement("foo", CSLLabel.CHAPTER, false)),
                NumberParser.parse("ch. foo"));
    }

    /**
     * Test if number ranges with labels can be parsed
     */
    @Test
    public void labelPlural() {
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", CSLLabel.CHAPTER, true)),
                NumberParser.parse("chaps. 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", CSLLabel.SECTION, true)),
                NumberParser.parse("secs. 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", CSLLabel.CHAPTER, true)),
                NumberParser.parse("chapters 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", CSLLabel.SECTION, true)),
                NumberParser.parse("sections 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10, 4", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 10, 4"));
        assertEquals(Collections.singletonList(
                new NumberElement("10–12", null, true)),
                NumberParser.parse("10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10, 12", null, true)),
                NumberParser.parse("10,12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10, 12", null, true)),
                NumberParser.parse("10,12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10 & 12", null, true)),
                NumberParser.parse("10 & 12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10 & 12", null, true)),
                NumberParser.parse("10&12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10 and 12", null, true)),
                NumberParser.parse("10 and 12"));
        assertEquals(Collections.singletonList(
                new NumberElement("2–3", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 2-3"));
        assertEquals(Collections.singletonList(
                new NumberElement("1, 2, 3", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 1,2,3"));
        assertEquals(Collections.singletonList(
                new NumberElement("1, 2, and 3", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 1,2, and 3"));
        assertEquals(Collections.singletonList(
                new NumberElement("1, 2, and 3", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 1,2, and 3,"));
    }

    /**
     * Test if complex strings consisting of multiple labels and numbers
     * can be parsed
     */
    @Test
    public void multipleLabels() {
        assertEquals(Arrays.asList(
                new NumberElement("10, ", CSLLabel.CHAPTER, false),
                new NumberElement("4", CSLLabel.SECTION, false)),
                NumberParser.parse(" ch. 10,  sec. 4  "));
        assertEquals(Arrays.asList(
                new NumberElement("10, ", CSLLabel.CHAPTER, false),
                new NumberElement("4", CSLLabel.SECTION, false)),
                NumberParser.parse("ch. 10 ,sec. 4"));
        assertEquals(Arrays.asList(
                new NumberElement("2–3, ", CSLLabel.CHAPTER, true),
                new NumberElement("4–5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2-3, p. 4-5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3, ", CSLLabel.CHAPTER, true),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2, 3, p. 4, 5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3, ", CSLLabel.CHAPTER, true),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2,3, p. 4,5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3; ", CSLLabel.CHAPTER, true),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2, 3; p. 4, 5"));
        assertEquals(Arrays.asList(
                new NumberElement("foo. bar, "),
                new NumberElement("2–3", CSLLabel.SECTION, true)),
                NumberParser.parse("foo. bar, sec. 2-3"));
        assertEquals(Arrays.asList(
                new NumberElement("cp. foo, "),
                new NumberElement("2–3", CSLLabel.SECTION, true)),
                NumberParser.parse("cp. foo, sec. 2-3"));
        assertEquals(Arrays.asList(
                new NumberElement("1, 2, and 3, ", CSLLabel.CHAPTER, true),
                new NumberElement("foo", CSLLabel.SECTION, false)),
                NumberParser.parse("ch. 1,2, and 3, sec. foo"));
        assertEquals(Arrays.asList(
                new NumberElement("1, 2, and 3, ", CSLLabel.CHAPTER, true),
                new NumberElement("foo bar", CSLLabel.SECTION, false)),
                NumberParser.parse("ch. 1,2, and 3, sec. foo bar"));
        assertEquals(Arrays.asList(
                new NumberElement("foo bar zoo, ", CSLLabel.SECTION, false),
                new NumberElement("dummy dummy", CSLLabel.CHAPTER, false)),
                NumberParser.parse("sec. foo   bar  zoo , ch. dummy dummy"));
        assertEquals(Arrays.asList(
                new NumberElement("5 foo, test sec. 1-5, ", CSLLabel.CHAPTER, false),
                new NumberElement("5, 3, 6", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 5 foo, test sec. 1-5, p. 5, 3, 6"));
    }

    /**
     * Test if subsequent elements with the same label are correctly merged
     */
    @Test
    public void merge() {
        assertEquals(Collections.singletonList(
                new NumberElement("10, 40", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 10, ch. 40"));
        assertEquals(Collections.singletonList(
                new NumberElement("10, 40", CSLLabel.SECTION, true)),
                NumberParser.parse("sec. 10, sec. 40"));
        assertEquals(Collections.singletonList(
                new NumberElement("10–12, 40", CSLLabel.SECTION, true)),
                NumberParser.parse("sec. 10-12, sec. 40"));
        assertEquals(Arrays.asList(
                new NumberElement("10, ", CSLLabel.CHAPTER, false),
                new NumberElement("40", CSLLabel.SECTION, false)),
                NumberParser.parse("ch. 10, sec. 40"));
        assertEquals(Arrays.asList(
                new NumberElement("10, 40, ", CSLLabel.CHAPTER, true),
                new NumberElement("2", CSLLabel.SECTION, false)),
                NumberParser.parse("ch. 10, ch. 40, sec. 2"));
        assertEquals(Arrays.asList(
                new NumberElement("10, ", CSLLabel.CHAPTER, false),
                new NumberElement("40, ", CSLLabel.SECTION, false),
                new NumberElement("2", CSLLabel.CHAPTER, false)),
                NumberParser.parse("ch. 10, sec. 40, ch. 2"));
    }
}
