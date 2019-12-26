package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;

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
        return buffer.toString();
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer, RenderContext ctx) {
        return "  <div class=\"csl-entry\">" + buffer.toString() + "</div>\n";
    }

    @Override
    public Bibliography makeBibliography(String[] entries) {
        return new Bibliography(entries, "<div class=\"csl-bib-body\">\n", "</div>",
                null, null, null, null, null, null, null);
    }
}
