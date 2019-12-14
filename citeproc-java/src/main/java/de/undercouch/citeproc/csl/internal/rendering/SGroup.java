package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SDelimitingRenderingElementContainer;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import org.w3c.dom.Node;

/**
 * A group of rendering elements from a style file
 * @author Michel Kraemer
 */
public class SGroup extends SDelimitingRenderingElementContainer implements SRenderingElement {
    private final Affixes affixes;

    /**
     * Creates the group from an XML node
     * @param node the XML node
     */
    public SGroup(Node node) {
        super(node, true);
        affixes = new Affixes(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.accept(super::render, ctx);
    }
}
