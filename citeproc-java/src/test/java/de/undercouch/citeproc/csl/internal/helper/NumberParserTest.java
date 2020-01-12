package de.undercouch.citeproc.csl.internal.helper;

import de.undercouch.citeproc.csl.CSLLabel;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NumberParserTest {
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
        assertEquals(Collections.singletonList(
                new NumberElement("foo", CSLLabel.CHAPTER, false)),
                NumberParser.parse("ch. foo"));
    }

    @Test
    public void range() {
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", null, true)),
                NumberParser.parse("10-12"));
        assertEquals(Arrays.asList(
                new NumberElement("10a, I", null, true),
                new NumberElement("; "),
                new NumberElement("10c, I", null, true)),
                NumberParser.parse("10a,I; 10c,I"));
    }

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
    }

    @Test
    public void labelPlural() {
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", CSLLabel.CHAPTER, true)),
                NumberParser.parse("chaps. 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", CSLLabel.SECTION, true)),
                NumberParser.parse("secs. 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", CSLLabel.CHAPTER, true)),
                NumberParser.parse("chapters 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", CSLLabel.SECTION, true)),
                NumberParser.parse("sections 10-12"));
        assertEquals(Collections.singletonList(
                new NumberElement("10, 4", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 10, 4"));
        assertEquals(Collections.singletonList(
                new NumberElement("10\u201312", null, true)),
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
                new NumberElement("2\u20133", CSLLabel.CHAPTER, true)),
                NumberParser.parse("ch. 2-3"));
    }

    @Test
    public void multipleLabels() {
        assertEquals(Arrays.asList(
                new NumberElement("10", CSLLabel.CHAPTER, false),
                new NumberElement(", "),
                new NumberElement("4", CSLLabel.SECTION, false)),
                NumberParser.parse(" ch. 10,  sec. 4  "));
        assertEquals(Arrays.asList(
                new NumberElement("2\u20133", CSLLabel.CHAPTER, true),
                new NumberElement(", "),
                new NumberElement("4\u20135", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2-3, p. 4-5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3", CSLLabel.CHAPTER, true),
                new NumberElement(", "),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2, 3, p. 4, 5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3", CSLLabel.CHAPTER, true),
                new NumberElement(", "),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2,3, p. 4,5"));
        assertEquals(Arrays.asList(
                new NumberElement("2, 3", CSLLabel.CHAPTER, true),
                new NumberElement("; "),
                new NumberElement("4, 5", CSLLabel.PAGE, true)),
                NumberParser.parse("ch. 2, 3; p. 4, 5"));
    }
}
