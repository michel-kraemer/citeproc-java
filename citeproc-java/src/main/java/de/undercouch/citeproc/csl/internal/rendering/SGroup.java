package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.CountingVariableListener;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SRenderingElementContainer;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A group of rendering elements from a style file
 * @author Michel Kraemer
 */
public class SGroup extends SRenderingElementContainer implements SRenderingElement {
    private final Affixes affixes;
    private final int formattingAttributes;
    private final String delimiter;

    /**
     * Creates the group from an XML node
     * @param node the XML node
     */
    public SGroup(Node node) {
        super(node);
        affixes = new Affixes(node);
        formattingAttributes = FormattingAttributes.of(node);
        delimiter = NodeHelper.getAttrValue(node, "delimiter");
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.accept(this::renderInternal, ctx);
    }

    private void renderInternal(RenderContext ctx) {
        // render elements in a separate context and count called variables
        RenderContext child = new RenderContext(ctx);
        CountingVariableListener vl = new CountingVariableListener();
        child.addVariableListener(vl);
        for (SRenderingElement e : elements) {
            RenderContext tmp = new RenderContext(child);
            e.render(tmp);

            if (!tmp.getResult().isEmpty()) {
                if (delimiter != null && !child.getResult().isEmpty()) {
                    child.emit(delimiter, Token.Type.DELIMITER);
                }
                child.emit(tmp.getResult());
            }
        }
        child.removeVariableListener(vl);

        // do not render the group if all called variables were empty
        boolean allEmpty = vl.getCalled() > 0 && vl.getCalled() == vl.getEmpty();

        if (!allEmpty && !child.getResult().isEmpty()) {
            ctx.emit(child.getResult(), formattingAttributes);
        }
    }
}
