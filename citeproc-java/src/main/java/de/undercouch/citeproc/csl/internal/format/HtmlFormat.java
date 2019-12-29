package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;
import org.apache.commons.text.StringEscapeUtils;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_ITALIC;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUP;

/**
 * The HTML output format
 * @author Michel Kraemer
 */
public class HtmlFormat extends BaseFormat {
    @Override
    public String getName() {
        return "html";
    }

    @Override
    protected String doFormatCitation(TokenBuffer buffer, RenderContext ctx) {
        return format(buffer);
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer, RenderContext ctx) {
        return "  <div class=\"csl-entry\">" + format(buffer) + "</div>\n";
    }

    @Override
    public Bibliography makeBibliography(String[] entries) {
        return new Bibliography(entries, "<div class=\"csl-bib-body\">\n", "</div>",
                null, null, null, null, null, null, null);
    }

    @Override
    protected String escape(String str) {
        return StringEscapeUtils.escapeHtml4(str);
    }

    @Override
    protected String openFontStyle(int fontStyle) {
        if (fontStyle == FS_ITALIC) {
            return "<span style=\"font-style: italic\">";
        } else {
            return "<span style=\"font-style: oblique\">";
        }
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        return "</span>";
    }

    @Override
    protected String openFontVariant(int fontVariant) {
        return "<span style=\"font-variant: small-caps\">";
    }

    @Override
    protected String closeFontVariant(int fontVariant) {
        return "</span>";
    }

    @Override
    protected String openFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "<span style=\"font-weight: bold\">";
        } else {
            return "<span style=\"font-weight: 100\">";
        }
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        return "</span>";
    }

    @Override
    protected String openTextDecoration(int textDecoration) {
        return "<span style=\"text-decoration: underline\">";
    }

    @Override
    protected String closeTextDecoration(int textDecoration) {
        return "</span>";
    }

    @Override
    protected String openVerticalAlign(int verticalAlign) {
        if (verticalAlign == VA_SUP) {
            return "<sup>";
        } else {
            return "<sub>";
        }
    }

    @Override
    protected String closeVerticalAlign(int verticalAlign) {
        if (verticalAlign == VA_SUP) {
            return "</sup>";
        } else {
            return "</sub>";
        }
    }
}
