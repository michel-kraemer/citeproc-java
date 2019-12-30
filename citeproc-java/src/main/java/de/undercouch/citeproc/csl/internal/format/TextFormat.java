package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;

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
        return format(buffer);
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer, RenderContext ctx) {
        if (!buffer.isEmpty()) {
            buffer.append("\n", Token.Type.TEXT);
        }

        return format(buffer);
    }

    @Override
    protected String doFormatLink(String text, String uri) {
        return text;
    }

    @Override
    public Bibliography makeBibliography(String[] entries,
            SBibliography bibliographyElement) {
        SecondFieldAlign sfa = bibliographyElement.getSecondFieldAlign();
        return new Bibliography(entries, null, null, null, null, null, null,
                null, null, sfa);
    }
}
