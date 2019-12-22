package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A container that renders a delimiter between its elements
 * @author Michel Kraemer
 */
public class SDelimitingRenderingElementContainer extends SRenderingElementContainer {
    private final String delimiter;
    private final boolean filterElementsWithEmptyVariables;

    /**
     * Construct the container from a given XML node
     * @param node the XML node
     */
    public SDelimitingRenderingElementContainer(Node node) {
        this(node, false);
    }

    /**
     * Construct the container from a given XML node
     * @param node the XML node
     * @param filterElementsWithEmptyVariables {@code true} if child elements
     * that only call empty variables should not be rendered at all
     */
    public SDelimitingRenderingElementContainer(Node node,
            boolean filterElementsWithEmptyVariables) {
        super(node);
        delimiter = NodeHelper.getAttrValue(node, "delimiter");
        this.filterElementsWithEmptyVariables = filterElementsWithEmptyVariables;
    }

    @Override
    public void render(RenderContext ctx) {
        boolean emitted = false;
        for (SRenderingElement e : elements) {
            // render the element in a separate context
            RenderContext child = new RenderContext(ctx);
            CountingVariableListener vl = new CountingVariableListener();
            child.addVariableListener(vl);
            e.render(child);
            child.removeVariableListener(vl);

            // count new number of called and empty variables
            boolean shouldBeFiltered = false;
            if (filterElementsWithEmptyVariables) {
                if (vl.getCalled() > 0 && vl.getCalled() == vl.getEmpty()) {
                    // all called variables were empty
                    // do not render this element
                    shouldBeFiltered = true;
                }
            }

            if (!shouldBeFiltered && !child.getResult().isEmpty()) {
                if (emitted && delimiter != null) {
                    ctx.emit(delimiter, Token.Type.DELIMITER);
                }
                ctx.emit(child.getResult());
                emitted = true;
            }
        }
    }
}
