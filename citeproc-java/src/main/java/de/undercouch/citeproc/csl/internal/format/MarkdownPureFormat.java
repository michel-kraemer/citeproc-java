package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;

/**
 * <p>The pure Markdown output format.</p>
 *
 * <p>Outputs pure Markdown markup without HTML tags. Use {@link MarkdownFormat}
 * for improved output.</p>
 *
 * @author Michel Kraemer
 */
public class MarkdownPureFormat extends BaseFormat {
    @Override
    public String getName() {
        return "markdown-pure";
    }

    @Override
    protected String doFormatCitation(TokenBuffer buffer, RenderContext ctx) {
        return format(buffer);
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index) {
        if (!buffer.isEmpty()) {
            buffer.append("\n\n", TEXT);
        }

        return format(buffer);
    }

    @Override
    protected String formatURL(String str) {
        return doFormatLink(str, str);
    }

    @Override
    protected String formatDOI(String str) {
        String uri = addDOIPrefix(str);
        return doFormatLink(str, uri);
    }

    @Override
    protected String doFormatLink(String text, String uri) {
        text = text.replace("[", "\\[").replace("]", "\\]");
        uri = uri.replace("<", "\\<").replace(">", "\\>");
        return "[" + text + "](<" + uri + ">)";
    }

    @Override
    protected String escape(String str) {
        return str.replaceAll("[!\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]", "\\\\$0");
    }

    @Override
    public Bibliography makeBibliography(String[] entries,
            SBibliography bibliographyElement) {
        SecondFieldAlign sfa = bibliographyElement.getSecondFieldAlign();
        return new Bibliography(entries, null, null, null, null, null, null,
                null, null, sfa);
    }

    @Override
    protected String openFontStyle(int fontStyle) {
        return "*";
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        return "*";
    }

    @Override
    protected String openFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "**";
        }
        return null;
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "**";
        }
        return null;
    }

    @Override
    protected String openDisplayGroup(DisplayGroupToken.Type type) {
        switch (type) {
            case BLOCK:
            case INDENT:
                return "\n";
            default:
                break;
        }
        return null;
    }
}
