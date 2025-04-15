package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import org.junit.Test;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.URL;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link HtmlFormat}
 * @author Michel Kraemer
 */
public class HtmlFormatTest {
    /**
     * Format a simple string
     */
    @Test
    public void formatSimple() {
        HtmlFormat f = new HtmlFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("Hello world", TEXT);
        String r = f.format(buf);
        assertEquals("Hello world", r);
    }

    /**
     * Format a simple string with some formatting attributes
     */
    @Test
    public void formatFormattingAttributes() {
        HtmlFormat f = new HtmlFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("Hello ", TEXT);

        int fa = 0;
        fa = FormattingAttributes.merge(fa,
                FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC));
        fa = FormattingAttributes.merge(fa,
                FormattingAttributes.ofFontWeight(FormattingAttributes.FW_BOLD));

        buf.append("world", TEXT, fa);
        String r = f.format(buf);

        assertEquals("Hello <span style=\"font-style: italic\">" +
                "<span style=\"font-weight: bold\">world</span></span>", r);
    }

    /**
     * Format a simple string with a formatting attribute that is closed and
     * then opened again with another value
     */
    @Test
    public void formatFormattingAttributesOpenClose() {
        int faItalic = FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC);
        int faOblique = FormattingAttributes.ofFontStyle(FormattingAttributes.FS_OBLIQUE);

        HtmlFormat f = new HtmlFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("Hello ", TEXT, faItalic);
        buf.append("world", TEXT, faOblique);
        String r = f.format(buf);

        assertEquals("<span style=\"font-style: italic\">Hello " +
                "</span><span style=\"font-style: oblique\">world</span>", r);
    }

    /**
     * Format a simple string with nested formatting attributes, where first
     * attribute A is opened, then attribute B is opened, then A is closed
     * again, and finally B is closed. Also test with even deeper nested
     * attributes.
     */
    @Test
    public void formatFormattingAttributesNested() {
        HtmlFormat f = new HtmlFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("Undefined ", TEXT);

        int faItalic = FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC);
        int faBold = FormattingAttributes.ofFontWeight(FormattingAttributes.FW_BOLD);
        int faBoth = FormattingAttributes.merge(faItalic, faBold);

        buf.append("Italic ", TEXT, faItalic);
        buf.append("Both ", TEXT, faBoth);
        buf.append("Bold ", TEXT, faBold);

        String r = f.format(buf);
        assertEquals("Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>", r);

        buf.append("Undefined", TEXT);

        r = f.format(buf);
        assertEquals("Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined", r);

        TokenBuffer outer = new TokenBuffer();
        int faUnderline = FormattingAttributes.ofTextDecoration(
                FormattingAttributes.TD_UNDERLINE);
        outer.append("Outer Start ", TEXT, faUnderline);
        buf.getTokens().stream()
                .map(t -> t.wrapFormattingAttributes(faUnderline))
                .forEach(outer::append);

        r = f.format(outer);
        assertEquals("<span style=\"text-decoration: underline\">Outer Start " +
                "Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined" +
                "</span>", r);

        outer.append(" Outer End", TEXT, faUnderline);

        r = f.format(outer);
        assertEquals("<span style=\"text-decoration: underline\">Outer Start " +
                "Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined " +
                "Outer End</span>", r);
    }

    /**
     * Check if URLs are correctly formatted and if JavaScript injection is
     * prevented
     */
    @Test
    public void formatLinkWithJavascript() {
        HtmlFormat f = new HtmlFormat();
        f.setConvertLinks(true);
        TokenBuffer buf = new TokenBuffer();
        buf.append("Hello ", TEXT);

        int fa = FormattingAttributes.merge(0,
                FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC));

        buf.append("http://example.com", URL, fa);
        buf.append("https://example.com", URL, fa);
        buf.append("javascript:alert('Hello world')", URL, fa);
        buf.append("ftp://example.com", URL, fa);
        buf.append("mailto:example@example.com", URL, fa);
        String r = f.format(buf);

        assertEquals("Hello <span style=\"font-style: italic\">" +
                "<a href=\"http://example.com\">http://example.com</a>" +
                "<a href=\"https://example.com\">https://example.com</a>" +
                "javascript:alert('Hello world')" +
                "<a href=\"ftp://example.com\">ftp://example.com</a>" +
                "<a href=\"mailto:example@example.com\">mailto:example@example.com</a></span>", r);
    }
}
