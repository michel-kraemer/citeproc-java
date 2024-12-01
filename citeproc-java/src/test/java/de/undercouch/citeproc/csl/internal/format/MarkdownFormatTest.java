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
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MarkdownFormat}
 * @author Michel Kraemer
 */
public class MarkdownFormatTest extends MarkdownPureFormatTest {
    protected BaseFormat createMarkdownFormat() {
        return new MarkdownFormat();
    }

    /**
     * Test formatting attributes
     */
    @Test
    public void formattingAttributes() {
        BaseFormat f = createMarkdownFormat();
        TokenBuffer buf = new TokenBuffer();
        buf.append("italic", TEXT, FormattingAttributes.ofFontStyle(FS_ITALIC));
        buf.append("oblique", TEXT, FormattingAttributes.ofFontStyle(FS_OBLIQUE));
        buf.append("smallcaps", TEXT, FormattingAttributes.ofFontVariant(FV_SMALLCAPS));
        buf.append("bold", TEXT, FormattingAttributes.ofFontWeight(FW_BOLD));
        buf.append("underline", TEXT, FormattingAttributes.ofTextDecoration(TD_UNDERLINE));
        buf.append("superscript", TEXT, FormattingAttributes.ofVerticalAlign(VA_SUP));
        buf.append("subscript", TEXT, FormattingAttributes.ofVerticalAlign(VA_SUB));

        String r = f.format(buf);
        assertEquals("*italic*<span style=\"font-style: oblique\">oblique</span>" +
                "<span style=\"font-variant: small-caps\">smallcaps</span>" +
                "**bold**<span style=\"text-decoration: underline\">underline</span>" +
                "<sup>superscript</sup><sub>subscript</sub>", r);
    }
}
