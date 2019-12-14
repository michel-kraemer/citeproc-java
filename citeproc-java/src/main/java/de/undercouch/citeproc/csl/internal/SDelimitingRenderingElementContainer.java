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
        if (delimiter == null) {
            super.render(ctx);
        } else {
            boolean emitted = false;
            for (SRenderingElement e : elements) {
                // save current number of called and empty variables
                int variablesCalled = ctx.getNumberOfCalledVariables();
                int variablesEmpty = ctx.getNumberOfEmptyVariables();
                int variablesDiff = variablesCalled - variablesEmpty;

                // render the element in a separate context
                RenderContext child = new RenderContext(ctx);
                e.render(child);

                // count new number of called and empty variables
                boolean shouldBeFiltered = false;
                if (filterElementsWithEmptyVariables) {
                    int newVariablesCalled = ctx.getNumberOfCalledVariables();
                    int newVariablesEmpty = ctx.getNumberOfEmptyVariables();
                    int newVariablesDiff = newVariablesCalled - newVariablesEmpty;
                    if (newVariablesCalled > variablesCalled && newVariablesDiff == variablesDiff) {
                        // all called variables were empty
                        // do not render this element
                        shouldBeFiltered = true;
                    }
                }

                if (!shouldBeFiltered && !child.getResult().isEmpty()) {
                    if (emitted) {
                        ctx.emit(delimiter);
                    }
                    ctx.emit(child.getResult());
                    emitted = true;
                }
            }
        }
    }
}
