package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.CollectingVariableListener;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SRenderingElementContainer;
import org.w3c.dom.Node;

/**
 * A substitute element from a style file
 * @author Michel Kraemer
 */
public class SSubstitute extends SRenderingElementContainer {
    /**
     * Construct the substitute from an XML node
     * @param node the XML node
     * @param parentNamesNode the parent names element that contains this
     * substitute element
     */
    public SSubstitute(Node node, Node parentNamesNode) {
        super(node);

        // replace names elements so they inherit the attributes and children
        // of `parentNamesNode`
        for (int i = 0; i < elements.size(); ++i) {
            SRenderingElement e = elements.get(i);
            if (e instanceof SNames) {
                e = new SNames(parentNamesNode, ((SNames)e).getVariable(), false);
                elements.set(i, e);
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
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
