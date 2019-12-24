package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A layout element inside a citation element in a style file
 * @author Michel Kraemer
 */
public class SCitationLayout extends SLayout {
    private CSLCitationItem[] citationItems;
    private final String delimiter;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SCitationLayout(Node node) {
        super(node);
        delimiter = NodeHelper.getAttrValue(node, "delimiter");
    }

    /**
     * Set the citation items to render
     * @param citationItems the citation items
     */
    public void setCitationItems(CSLCitationItem[] citationItems) {
        this.citationItems = citationItems;
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(this::renderInternal).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        if (citationItems == null) {
            return;
        }

        RenderContext tmp = new RenderContext(ctx);
        for (CSLCitationItem item : citationItems) {
            RenderContext innerTmp = new RenderContext(ctx, item.getItemData());
            super.render(innerTmp);

            if (delimiter != null && !tmp.getResult().isEmpty()) {
                tmp.emit(delimiter, Token.Type.DELIMITER);
            }

            tmp.emit(innerTmp.getResult());
        }
        ctx.emit(tmp.getResult());
    }
}
