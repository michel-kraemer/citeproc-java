package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.rendering.SNameInheritableAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.output.SecondFieldAlign;
import org.w3c.dom.Node;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private final SSort sort;
    private final SLayout layout;
    private final SecondFieldAlign secondFieldAlign;
    private final SNameInheritableAttributes inheritableNameAttributes;

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

        String strSecondFieldAlign = NodeHelper.getAttrValue(node, "second-field-align");
        if ("flush".equals(strSecondFieldAlign)) {
            secondFieldAlign = SecondFieldAlign.FLUSH;
        } else if ("margin".equals(strSecondFieldAlign)) {
            secondFieldAlign = SecondFieldAlign.MARGIN;
        } else {
            secondFieldAlign = SecondFieldAlign.FALSE;
        }

        inheritableNameAttributes = new SNameInheritableAttributes(node);
    }

    /**
     * Get the sort child element (if there is any)
     * @return the sort child element (or {@code null} if the bibliography
     * element does not have a sort child element)
     */
    public SSort getSort() {
        return sort;
    }

    /**
     * Get the property that specifies whether subsequent lines of
     * bibliographies should be aligned along the second field
     * @return the second-field-align property (never {@code null})
     */
    public SecondFieldAlign getSecondFieldAlign() {
        return secondFieldAlign;
    }

    @Override
    public void render(RenderContext ctx) {
        if (layout != null) {
            inheritableNameAttributes.wrap(layout::render).accept(ctx);
        }
    }
}
