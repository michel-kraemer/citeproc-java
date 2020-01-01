package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.CountingVariableListener;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SRenderingElementContainer;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A group of rendering elements from a style file
 * @author Michel Kraemer
 */
public class SGroup extends SRenderingElementContainer implements SRenderingElement {
    private final Affixes affixes;
    private final String delimiter;

    /**
     * Creates the group from an XML node
     * @param node the XML node
     */
    public SGroup(Node node) {
        super(node);
        affixes = new Affixes(node);
        delimiter = NodeHelper.getAttrValue(node, "delimiter");
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.accept(this::renderInternal, ctx);
    }

    private void renderInternal(RenderContext ctx) {
        boolean emitted = false;
        for (SRenderingElement e : elements) {
            // render the element in a separate context
            RenderContext child = new RenderContext(ctx);
            CountingVariableListener vl = new CountingVariableListener();
            child.addVariableListener(vl);
            e.render(child);
            child.removeVariableListener(vl);

            // do not render this element if all called variables where empty
            boolean allEmpty = vl.getCalled() > 0 && vl.getCalled() == vl.getEmpty();

            if (!allEmpty && !child.getResult().isEmpty()) {
                if (emitted && delimiter != null) {
                    ctx.emit(delimiter, Token.Type.DELIMITER);
                }
                ctx.emit(child.getResult());
                emitted = true;
            }
        }
    }
}
