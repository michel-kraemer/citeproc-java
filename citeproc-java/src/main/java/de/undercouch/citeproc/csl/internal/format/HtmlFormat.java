package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
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
    public String formatCitation(RenderContext ctx) {
        return ctx.getResult().toString();
    }

    @Override
    public String formatBibliographyEntry(RenderContext ctx) {
        return ctx.getResult().toString();
    }

    @Override
    public Bibliography makeBibliography(String[] entries) {
        return new Bibliography(entries);
    }
}
