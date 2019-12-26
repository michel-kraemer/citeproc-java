package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A layout element inside a citation element in a style file
 * @author Michel Kraemer
 */
public class SCitationLayout extends SRenderingElementContainer {
    protected final Affixes affixes;
    private final String delimiter;
    private CSLCitationItem[] citationItems;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SCitationLayout(Node node) {
        super(node);
        affixes = new Affixes(node);
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
        RenderContext tmp = new RenderContext(ctx);
        for (CSLCitationItem item : citationItems) {
            RenderContext innerTmp = new RenderContext(ctx, item.getItemData());
            super.render(innerTmp);

            if (delimiter != null && !tmp.getResult().isEmpty()) {
                tmp.emit(delimiter, Token.Type.DELIMITER);
            }

            if (item.getPrefix() != null) {
                tmp.emit(item.getPrefix(), Token.Type.PREFIX);
            }

            tmp.emit(innerTmp.getResult());

            if (item.getSuffix() != null) {
                tmp.emit(item.getSuffix(), Token.Type.SUFFIX);
            }
        }
        ctx.emit(tmp.getResult());
    }
}
