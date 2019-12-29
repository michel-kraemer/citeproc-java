package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;
import org.apache.commons.text.StringEscapeUtils;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_ITALIC;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_NORMAL;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_OBLIQUE;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FV_SMALLCAPS;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.TD_UNDERLINE;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_BASELINE;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUB;
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
        switch (fontStyle) {
            case FS_NORMAL:
                return "<span style=\"font-style: normal\">";
            case FS_ITALIC:
                return "<i>";
            case FS_OBLIQUE:
                return "<em>";
            default:
                return null;
        }
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        switch (fontStyle) {
            case FS_NORMAL:
                return "</span>";
            case FS_ITALIC:
                return "</i>";
            case FS_OBLIQUE:
                return "</em>";
            default:
                return null;
        }
    }

    @Override
    protected String openFontVariant(int fontVariant) {
        if (fontVariant == FV_SMALLCAPS) {
            return "<span style=\"font-variant: small-caps\">";
        }
        return null;
    }

    @Override
    protected String closeFontVariant(int fontVariant) {
        if (fontVariant == FV_SMALLCAPS) {
            return "</span>";
        }
        return null;
    }

    @Override
    protected String openFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "<b>";
        }
        return null;
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "</b>";
        }
        return null;
    }

    @Override
    protected String openTextDecoration(int textDecoration) {
        if (textDecoration == TD_UNDERLINE) {
            return "<span style=\"text-decoration: underline\">";
        }
        return null;
    }

    @Override
    protected String closeTextDecoration(int textDecoration) {
        if (textDecoration == TD_UNDERLINE) {
            return "</span>";
        }
        return null;
    }

    @Override
    protected String openVerticalAlign(int verticalAlign) {
        switch (verticalAlign) {
            case VA_BASELINE:
                return "<span style=\"vertical-align: baseline\">";
            case VA_SUP:
                return "<sup>";
            case VA_SUB:
                return "<sub>";
            default:
                return null;
        }
    }

    @Override
    protected String closeVerticalAlign(int verticalAlign) {
        switch (verticalAlign) {
            case VA_BASELINE:
                return "</span>";
            case VA_SUP:
                return "</sup>";
            case VA_SUB:
                return "</sub>";
            default:
                return null;
        }
    }
}
