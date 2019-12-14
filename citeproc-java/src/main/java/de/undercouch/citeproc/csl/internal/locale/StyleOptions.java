package de.undercouch.citeproc.csl.internal.locale;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * Global style rendering options
 * @author Michel Kraemer
 */
public class StyleOptions {
    private final boolean punctuationInQuote;

    /**
     * Default constructor
     */
    public StyleOptions() {
        punctuationInQuote = false;
    }

    /**
     * Parses the style options from an XML node
     * @param node the XML node
     */
    public StyleOptions(Node node) {
        punctuationInQuote = Boolean.parseBoolean(NodeHelper.getAttrValue(
                node, "punctuation-in-quote"));
    }

    /**
     * Returns the punctuation-in-quote flag
     * @return {@code true} if dots and commas should be rendered inside quotes
     */
    public boolean isPunctuationInQuote() {
        return punctuationInQuote;
    }
}
