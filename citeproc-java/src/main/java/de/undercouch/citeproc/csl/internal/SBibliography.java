package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private final SSort sort;
    private final SLayout layout;

    /**
     * Construct the bibliography element from an XML node
     * @param node the XML node
     */
    public SBibliography(Node node) {
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
            layout = new SLayout(layoutNode);
        }
    }

    /**
     * Get the sort child element (if there is any)
     * @return the sort child element (or {@code null} if the bibliography
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
