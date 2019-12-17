package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A names element from a style file
 * @author Michel Kraemer
 */
public class SNames implements SRenderingElement {
    private final SName name;
    private final Affixes affixes;

    /**
     * Creates the names element from an XML node
     * @param node the XML node
     */
    public SNames(Node node) {
        String variable = NodeHelper.getAttrValue(node, "variable");
        name = new SName(NodeHelper.findDirectChild(node, "name"), variable);
        affixes = new Affixes(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(name::render).accept(ctx);
    }
}
