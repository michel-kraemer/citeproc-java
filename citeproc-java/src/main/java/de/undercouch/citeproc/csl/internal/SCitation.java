package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A citation element from a style file
 * @author Michel Kraemer
 */
public class SCitation implements SElement {
    private final SSort sort;
    private final SCitationLayout layout;

    /**
     * Construct the citation element from an XML node
     * @param node the XML node
     */
    public SCitation(Node node) {
        Node sortNode = NodeHelper.findDirectChild(node, "sort");
        if (sortNode == null) {
            sort = null;
        } else {
            sort = new SSort(sortNode);
        }

        Node layoutNode = NodeHelper.findDirectChild(node, "layout");
        if (layoutNode == null) {
            layout = null;
        } else {
            layout = new SCitationLayout(layoutNode);
        }
    }

    /**
     * Get the sort child element (if there is any)
     * @return the sort child element (or {@code null} if the citation
     * element does not have a sort child element)
     */
    public SSort getSort() {
        return sort;
    }

    @Override
    public void render(RenderContext ctx) {
        if (layout != null) {
            layout.render(ctx);
        }
    }
}
