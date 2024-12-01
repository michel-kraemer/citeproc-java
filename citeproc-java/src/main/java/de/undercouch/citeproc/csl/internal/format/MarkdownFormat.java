package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;

/**
 * The Markdown output format
 * @author Michel Kraemer
 */
public class MarkdownFormat extends BaseFormat {
    @Override
    public String getName() {
        return "markdown";
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
}
