package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import org.w3c.dom.Node;

/**
 * A layout element in a style file
 * @author Michel Kraemer
 */
public class SLayout extends SRenderingElementContainer {
    private final Affixes affixes;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SLayout(Node node) {
        super(node);
        affixes = new Affixes(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(this::renderInternal).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        for (int i = 0; i < elements.size(); i++) {
            SRenderingElement e = elements.get(i);
            if (i == 0) {
                // render first field
                RenderContext tmp = new RenderContext(ctx);
                e.render(tmp);
                for (Token t : tmp.getResult().getTokens()) {
                    // set flag in token
                    Token nt = new Token.Builder(t)
                            .firstField(true)
                            .build();
                    ctx.emit(nt);
                }
            } else {
                e.render(ctx);
            }
        }
    }
}
