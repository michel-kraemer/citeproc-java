package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A names element from a style file
 * @author Michel Kraemer
 */
public class SNames implements SRenderingElement {
    private final SName name;

    /**
     * Creates the names element from an XML node
     * @param node the XML node
     */
    public SNames(Node node) {
        String variable = NodeHelper.getAttrValue(node, "variable");
        if (variable == null || variable.isEmpty()) {
            throw new IllegalStateException("Names element does not select a variable");
        }

        Node nameNode = NodeHelper.findDirectChild(node, "name");
        if (nameNode == null) {
            throw new IllegalStateException("Names element does not contain " +
                    "a name element");
        }
        this.name = new SName(nameNode, variable);
    }

    @Override
    public void render(RenderContext ctx) {
        name.render(ctx);
    }
}
