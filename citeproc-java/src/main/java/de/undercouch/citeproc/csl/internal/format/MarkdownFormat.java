package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_ITALIC;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUP;

/**
 * <p>The Markdown output format.</p>
 *
 * <p>Adheres to the <a href="https://spec.commonmark.org/">CommonMark
 * specification</a>. Use {@link MarkdownPureFormat} for output that does not
 * contain HTML tags.</p>
 *
 * @author Michel Kraemer
 */
public class MarkdownFormat extends MarkdownPureFormat {
    @Override
    public String getName() {
        return "markdown";
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index) {
        String r = format(buffer);
        if (!buffer.isEmpty()) {
            r += "<br />\n";
        }
        return r;
    }

    @Override
    protected String openFontStyle(int fontStyle) {
        if (fontStyle == FS_ITALIC) {
            return "*";
        } else {
            return "<span style=\"font-style: oblique\">";
        }
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        if (fontStyle == FS_ITALIC) {
            return "*";
        } else {
            return "</span>";
        }
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
            return "**";
        } else {
            return "<span style=\"font-weight: 100\">";
        }
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "**";
        } else {
            return "</span>";
        }
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

    @Override
    protected String openDisplayGroup(DisplayGroupToken.Type type) {
        switch (type) {
            case BLOCK:
            case INDENT:
                return "<br />";
            default:
                break;
        }
        return null;
    }
}
