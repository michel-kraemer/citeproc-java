package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.Formatting;
import de.undercouch.citeproc.output.Bibliography;
import org.apache.commons.text.StringEscapeUtils;

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
    protected String openFontStyle(Formatting.FontStyle fontStyle) {
        switch (fontStyle) {
            case NORMAL:
                return "<span style=\"font-style: normal\">";
            case ITALIC:
                return "<i>";
            case OBLIQUE:
                return "<em>";
            default:
                return null;
        }
    }

    @Override
    protected String closeFontStyle(Formatting.FontStyle fontStyle) {
        switch (fontStyle) {
            case NORMAL:
                return "</span>";
            case ITALIC:
                return "</i>";
            case OBLIQUE:
                return "</em>";
            default:
                return null;
        }
    }

    @Override
    protected String openFontVariant(Formatting.FontVariant fontVariant) {
        if (fontVariant == Formatting.FontVariant.SMALL_CAPS) {
            return "<span style=\"font-variant: small-caps\">";
        }
        return null;
    }

    @Override
    protected String closeFontVariant(Formatting.FontVariant fontVariant) {
        if (fontVariant == Formatting.FontVariant.SMALL_CAPS) {
            return "</span>";
        }
        return null;
    }

    @Override
    protected String openFontWeight(Formatting.FontWeight fontWeight) {
        if (fontWeight == Formatting.FontWeight.BOLD) {
            return "<b>";
        }
        return null;
    }

    @Override
    protected String closeFontWeight(Formatting.FontWeight fontWeight) {
        if (fontWeight == Formatting.FontWeight.BOLD) {
            return "</b>";
        }
        return null;
    }

    @Override
    protected String openTextDecoration(Formatting.TextDecoration textDecoration) {
        if (textDecoration == Formatting.TextDecoration.UNDERLINE) {
            return "<span style=\"text-decoration: underline\">";
        }
        return null;
    }

    @Override
    protected String closeTextDecoration(Formatting.TextDecoration textDecoration) {
        if (textDecoration == Formatting.TextDecoration.UNDERLINE) {
            return "</span>";
        }
        return null;
    }

    @Override
    protected String openVerticalAlign(Formatting.VerticalAlign verticalAlign) {
        switch (verticalAlign) {
            case SUB:
                return "<sub>";
            case SUP:
                return "<sup>";
            default:
                return null;
        }
    }

    @Override
    protected String closeVerticalAlign(Formatting.VerticalAlign verticalAlign) {
        switch (verticalAlign) {
            case SUB:
                return "</sub>";
            case SUP:
                return "</sup>";
            default:
                return null;
        }
    }
}
