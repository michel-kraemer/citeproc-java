package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import org.junit.Test;

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
        buf.append("Hello world", Token.Type.TEXT);
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
        buf.append("Hello ", Token.Type.TEXT);

        int fa = 0;
        fa = FormattingAttributes.merge(fa,
                FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC));
        fa = FormattingAttributes.merge(fa,
                FormattingAttributes.ofFontWeight(FormattingAttributes.FW_BOLD));

        buf.append("world", Token.Type.TEXT, fa);
        String r = f.format(buf);

        assertEquals("Hello <span style=\"font-style: italic\">" +
                "<span style=\"font-weight: bold\">world</span></span>", r);
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
        buf.append("Undefined ", Token.Type.TEXT);

        int faItalic = FormattingAttributes.ofFontStyle(FormattingAttributes.FS_ITALIC);
        int faBold = FormattingAttributes.ofFontWeight(FormattingAttributes.FW_BOLD);
        int faBoth = FormattingAttributes.merge(faItalic, faBold);

        buf.append("Italic ", Token.Type.TEXT, faItalic);
        buf.append("Both ", Token.Type.TEXT, faBoth);
        buf.append("Bold ", Token.Type.TEXT, faBold);

        String r = f.format(buf);
        assertEquals("Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>", r);

        buf.append("Undefined", Token.Type.TEXT);

        r = f.format(buf);
        assertEquals("Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined", r);

        TokenBuffer outer = new TokenBuffer();
        int faUnderline = FormattingAttributes.ofTextDecoration(
                FormattingAttributes.TD_UNDERLINE);
        outer.append("Outer Start ", Token.Type.TEXT, faUnderline);
        buf.getTokens().stream()
                .map(t -> new Token.Builder(t)
                        .mergeFormattingAttributes(faUnderline)
                        .build())
                .forEach(outer::append);

        r = f.format(outer);
        assertEquals("<span style=\"text-decoration: underline\">Outer Start " +
                "Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined" +
                "</span>", r);

        outer.append(" Outer End", Token.Type.TEXT, faUnderline);

        r = f.format(outer);
        assertEquals("<span style=\"text-decoration: underline\">Outer Start " +
                "Undefined <span style=\"font-style: italic\">Italic " +
                "<span style=\"font-weight: bold\">Both </span></span>" +
                "<span style=\"font-weight: bold\">Bold </span>Undefined " +
                "Outer End</span>", r);
    }
}
