package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import org.w3c.dom.Node;

/**
 * A layout element in a style file
 * @author Michel Kraemer
 */
public class SLayout extends SRenderingElementContainer {
    private final Affixes affixes;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SLayout(Node node) {
        super(node);
        affixes = new Affixes(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.accept(super::render, ctx);
    }
}
