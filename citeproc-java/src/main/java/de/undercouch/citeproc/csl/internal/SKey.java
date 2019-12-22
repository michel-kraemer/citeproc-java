package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A sort key inside a sort element
 * @author Michel Kraemer
 */
public class SKey implements SRenderingElement {
    private final String macro;
    private final String variable;
    private final int sort;

    /**
     * Creates the key from an XML node
     * @param node the XML node
     */
    public SKey(Node node) {
        macro = NodeHelper.getAttrValue(node, "macro");
        variable = NodeHelper.getAttrValue(node, "variable");

        String strSort = NodeHelper.getAttrValue(node, "sort");
        if ("descending".equals(strSort)) {
            sort = -1;
        } else {
            sort = 1;
        }
    }

    /**
     * Get the sort direction
     * @return the sort direction ({@code 1} for ascending and {@code -1} for
     * descending)
     */
    public int getSort() {
        return sort;
    }

    @Override
    public void render(RenderContext ctx) {
        if (macro != null) {
            ctx.getMacro(macro).render(ctx);
        }
    }
}
