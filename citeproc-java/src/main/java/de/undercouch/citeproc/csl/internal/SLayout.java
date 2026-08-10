package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import de.undercouch.citeproc.csl.internal.token.Token;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A layout element in a style file
 * @author Michel Kraemer
 */
public class SLayout extends SRenderingElementContainerElement {
    private final Affixes affixes;
    private final int formattingAttributes;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SLayout(Node node) {
        super(node);
        affixes = new Affixes(node);
        formattingAttributes = FormattingAttributes.of(node);
    }

    @Override
    public void render(RenderContext ctx) {
        RenderContext tmp = new RenderContext(ctx);
        List<SRenderingElement> elements = getElements(ctx);
        for (int i = 0; i < elements.size(); i++) {
            SRenderingElement e = elements.get(i);
            if (i == 0) {
                // render first field
                RenderContext innerTmp = new RenderContext(tmp);
                e.render(innerTmp);
                for (Token t : innerTmp.getResult().getTokens()) {
                    // set flag in token
                    tmp.emit(t.copyWithFirstField(true));
                }
            } else {
                e.render(tmp);
            }
        }
        TokenBuffer buffer = tmp.getResult();
        if (buffer.isEmpty()) {
            return;
        }

        // In contrast to all other elements, affixes on cs:layout are within
        // the scope of the formatting attributes set on the same element.
        // Apply them before the formatting attributes are wrapped around the
        // rendered tokens.
        boolean firstField = buffer.getTokens().get(0).isFirstField();
        affixes.applyTo(buffer);
        if (firstField) {
            // the prefix has been prepended to the first field, so it needs to
            // be marked as such too (otherwise second-field-align would break)
            List<Token> tokens = buffer.getTokens();
            tokens.set(0, tokens.get(0).copyWithFirstField(true));
        }

        ctx.emit(buffer, formattingAttributes);
    }
}
