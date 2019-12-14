package de.undercouch.citeproc.csl.internal.rendering.condition;

import de.undercouch.citeproc.csl.internal.RenderContext;
import org.w3c.dom.Node;

/**
 * A conditional element that always matches
 * @author Michel Kraemer
 */
public class SElse extends SCondition {
    /**
     * Create the element from an XML node
     * @param node the XML node
     */
    public SElse(Node node) {
        super(node);
    }

    @Override
    public boolean matches(RenderContext ctx) {
        return true;
    }
}
