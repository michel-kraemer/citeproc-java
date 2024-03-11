package de.undercouch.citeproc.csl.internal.locale;

import de.undercouch.citeproc.csl.internal.rendering.SDatePart;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * A localized date format
 * @author Michel Kraemer
 */
public class LDate {
    private final String form;
    private final String delimiter;
    private final List<SDatePart> dateParts = new ArrayList<>();

    /**
     * Construct the date format from an XML node
     * @param node the XML node
     */
    public LDate(Node node) {
        this.form = NodeHelper.getAttrValue(node, "form");
        this.delimiter = NodeHelper.getAttrValue(node, "delimiter");

        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); ++j) {
            Node c = children.item(j);
            if ("date-part".equals(c.getNodeName())) {
                dateParts.add(new SDatePart(c));
            }
        }
    }

    /**
     * Get the date form (text or numerical)
     * @return the form
     */
    public String getForm() {
        return form;
    }

    /**
     * An optional delimiter for the elements returned by {@link #getDateParts()}
     * @return a delimiter or {@code null} if no delimiter should be used
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Get the date-part elements to render
     * @return the elements
     */
    public List<SDatePart> getDateParts() {
        return dateParts;
    }
}
