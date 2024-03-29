package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;

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
    protected String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index) {
        if (!buffer.isEmpty()) {
            buffer.append("\n", TEXT);
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

    @Override
    protected String openDisplayGroup(DisplayGroupToken.Type type) {
        switch (type) {
            case BLOCK:
                return "\n";
            case INDENT:
                return "\n    ";
            default:
                break;
        }
        return null;
    }
}
