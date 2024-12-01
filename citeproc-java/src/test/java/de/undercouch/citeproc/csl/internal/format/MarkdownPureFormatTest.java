package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import org.junit.Test;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_ITALIC;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_OBLIQUE;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FV_SMALLCAPS;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.TD_UNDERLINE;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUB;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUP;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.DOI;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.URL;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MarkdownPureFormat}
 * @author Michel Kraemer
 */
public class MarkdownPureFormatTest {
    protected BaseFormat createMarkdownFormat() {
        return new MarkdownPureFormat();
    }

    /**
     * Format a simple string
     */
    @Test
    public void formatSimple() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("Hello world", TEXT);
        String r = f.format(buf);
        assertEquals("Hello world", r);
    }

    /**
     * Test if punctuation characters are escaped
     */
    @Test
    public void escape() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("1. this is not a list! " +
                "[and this is not a URL](http://example.com). " +
                "*not emphasized* and **not bold** " +
                "Not an HTML entity &amp; " +
                "<em>Not an HTML tag</em>", URL);

        String r = f.format(buf);
        assertEquals("1\\. this is not a list\\! " +
                "\\[and this is not a URL\\]\\(http\\:\\/\\/example\\.com\\)\\. " +
                "\\*not emphasized\\* and \\*\\*not bold\\*\\* " +
                "Not an HTML entity \\&amp\\; " +
                "\\<em\\>Not an HTML tag\\<\\/em\\>", r);
    }

    /**
     * Test if URLs are formatted correctly
     */
    @Test
    public void formatURL() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("example.com", URL);

        String r = f.format(buf);
        assertEquals("example\\.com", r);

        f.setConvertLinks(true);
        r = f.format(buf);
        assertEquals("[example.com](<example.com>)", r);

        TokenBuffer buf2 = new TokenBuffer();
        buf2.append("https://example.com/a<)>b[c*]", URL);

        r = f.format(buf2);
        assertEquals("[https://example.com/a<)>b\\[c*\\]](<https://example.com/a\\<)\\>b[c*]>)", r);
    }

    /**
     * Test if DOIs are formatted correctly
     */
    @Test
    public void formatDOI() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("00.0000/00000000", DOI);

        String r = f.format(buf);
        assertEquals("00\\.0000\\/00000000", r);

        f.setConvertLinks(true);
        r = f.format(buf);
        assertEquals("[00.0000/00000000](<https://doi.org/00.0000/00000000>)", r);
    }

    /**
     * Test formatting attributes
     */
    @Test
    public void formattingAttributes() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("italic", TEXT, FormattingAttributes.ofFontStyle(FS_ITALIC));
        buf.append(" ", TEXT); // italic and oblique must be separated
        buf.append("oblique", TEXT, FormattingAttributes.ofFontStyle(FS_OBLIQUE));
        buf.append("smallcaps", TEXT, FormattingAttributes.ofFontVariant(FV_SMALLCAPS));
        buf.append("bold", TEXT, FormattingAttributes.ofFontWeight(FW_BOLD));
        buf.append("underline", TEXT, FormattingAttributes.ofTextDecoration(TD_UNDERLINE));
        buf.append("superscript", TEXT, FormattingAttributes.ofVerticalAlign(VA_SUP));
        buf.append("subscript", TEXT, FormattingAttributes.ofVerticalAlign(VA_SUB));

        String r = f.format(buf);
        assertEquals("*italic* *oblique*smallcaps**bold**underline" +
                "superscriptsubscript", r);
    }
}
