package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A macro in a style file
 * @author Michel Kraemer
 */
public class SMacro extends SRenderingElementContainer {
    private String name;

    /**
     * Construct the macro from an XML node
     * @param node the XML node
     */
    public SMacro(Node node) {
        super(node);
        name = NodeHelper.getAttrValue(node, "name");
    }

    public String getName() {
        return name;
    }
}
