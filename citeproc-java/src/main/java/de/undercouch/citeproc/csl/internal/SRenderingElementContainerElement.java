package de.undercouch.citeproc.csl.internal;

import org.w3c.dom.Node;

/**
 * A container for style elements that is itself also a renderable element.
 * Renders all elements at once.
 * @author Michel Kraemer
 */
public class SRenderingElementContainerElement extends SRenderingElementContainer implements SElement {
    /**
     * Construct the container from an XML node
     * @param node the XML node
     */
    public SRenderingElementContainerElement(Node node) {
        super(node);
    }

    @Override
    public void render(RenderContext ctx) {
        getElements(ctx).forEach(e -> e.render(ctx));
    }
}
