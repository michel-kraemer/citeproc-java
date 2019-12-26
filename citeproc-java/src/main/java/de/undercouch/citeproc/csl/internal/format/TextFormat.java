package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;

/**
 * The text output format
 * @author Michel Kraemer
 */
public class TextFormat extends BaseFormat {
    @Override
    public String getName() {
        return "text";
    }

    @Override
    protected String doFormatCitation(TokenBuffer buffer, RenderContext ctx) {
        return buffer.toString();
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer, RenderContext ctx) {
        if (!buffer.isEmpty()) {
            buffer.append("\n", Token.Type.TEXT);
        }

        return buffer.toString();
    }

    @Override
    public Bibliography makeBibliography(String[] entries) {
        return new Bibliography(entries);
    }
}
