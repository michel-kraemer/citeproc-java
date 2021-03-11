package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import org.w3c.dom.Node;

/**
 * A layout element in a style file
 * @author Michel Kraemer
 */
public class SLayout extends SRenderingElementContainer {
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
        affixes.wrap(this::renderInternal).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        RenderContext tmp = new RenderContext(ctx);
        for (int i = 0; i < elements.size(); i++) {
            SRenderingElement e = elements.get(i);
            if (i == 0) {
                // render first field
                RenderContext innerTmp = new RenderContext(tmp);
                e.render(innerTmp);
                for (Token t : innerTmp.getResult().getTokens()) {
                    // set flag in token
                    Token nt = new Token.Builder(t)
                            .firstField(true)
                            .build();
                    tmp.emit(nt);
                }
            } else {
                e.render(tmp);
            }
        }
        ctx.emit(tmp.getResult(), formattingAttributes);
    }
}
