package de.undercouch.citeproc.csl.internal;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private SLayout layout;

    /**
     * Construct the bibliography element from an XML node
     * @param node the XML node
     */
    public SBibliography(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            if ("layout".equals(c.getNodeName())) {
                layout = new SLayout(c);
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        if (layout != null) {
            layout.render(ctx);
        }
    }
}
