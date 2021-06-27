package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.CollectingVariableListener;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SRenderingElementContainerElement;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * A substitute element from a style file
 * @author Michel Kraemer
 */
public class SSubstitute extends SRenderingElementContainerElement {
    private final Node parentNamesNode;

    /**
     * Construct the substitute from an XML node
     * @param node the XML node
     * @param parentNamesNode the parent names element that contains this
     * substitute element
     */
    public SSubstitute(Node node, Node parentNamesNode) {
        super(node);
        this.parentNamesNode = parentNamesNode;
    }

    @Override
    public void render(RenderContext ctx) {
        List<SRenderingElement> originalElements = getElements(ctx);

        // replace names elements so they inherit the attributes and children
        // of `parentNamesNode`
        List<SRenderingElement> elements = new ArrayList<>(originalElements.size());
        for (SRenderingElement e : originalElements) {
            if (e instanceof SNames) {
                e = new SNames(parentNamesNode, ((SNames)e).getVariableAttribute(), false);
            }
            elements.add(e);
        }

        RenderContext tmp = new RenderContext(ctx);
        for (SRenderingElement e : elements) {
            // render substitute and collect fetch variables
            CollectingVariableListener vl = new CollectingVariableListener();
            tmp.addVariableListener(vl);
            e.render(tmp);
            tmp.removeVariableListener(vl);

            if (!tmp.getResult().isEmpty()) {
                // The render result was not empty. Suppress fetched variables
                // for the rest of the output and emit the result. Then stop
                // rendering children.
                for (String variable : vl.getCalled()) {
                    ctx.suppressVariable(variable);
                }
                ctx.emit(tmp.getResult());
                break;
            }
        }
    }
}
