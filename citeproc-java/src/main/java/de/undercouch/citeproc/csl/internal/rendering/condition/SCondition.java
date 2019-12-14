package de.undercouch.citeproc.csl.internal.rendering.condition;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SRenderingElementContainer;
import org.w3c.dom.Node;

/**
 * A conditional element from a style file
 * @author Michel Kraemer
 */
public abstract class SCondition extends SRenderingElementContainer {
    /**
     * Construct the conditional element from an XML node
     * @param node the XML node
     */
    public SCondition(Node node) {
        super(node);
    }

    /**
     * Check if the condition applies
     * @param ctx the current render context
     * @return {@code true} if the condition applies, {@code false} otherwise
     */
    public abstract boolean matches(RenderContext ctx);
}
