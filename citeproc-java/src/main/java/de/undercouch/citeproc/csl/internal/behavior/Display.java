package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * Represents the 'display' attribute that can be used to structure
 * bibliographic entries into one or more text blocks.
 * @author Michel Kraemer
 */
public enum Display {
    UNDEFINED,
    BLOCK,
    LEFT_MARGIN,
    RIGHT_INLINE,
    INDENT;

    /**
     * Create displays attributes from an XML node
     * @param node the XML node to parse
     * @return the display attributes
     */
    public static Display of(Node node) {
        String strDisplay = NodeHelper.getAttrValue(node, "display");
        if (strDisplay != null) {
            switch (strDisplay) {
                case "block":
                    return BLOCK;
                case "left-margin":
                    return LEFT_MARGIN;
                case "right-inline":
                    return RIGHT_INLINE;
                case "indent":
                    return INDENT;
                default:
                    break;
            }
        }
        return UNDEFINED;
    }
}
