package de.undercouch.citeproc.csl.internal.rendering;

import org.w3c.dom.Node;

/**
 * A number element from a style file
 * @author Michel Kraemer
 */
public class SNumber extends SText {
    /**
     * Creates the number element from an XML node
     * @param node the XML node
     */
    public SNumber(Node node) {
        super(node);
    }
}
