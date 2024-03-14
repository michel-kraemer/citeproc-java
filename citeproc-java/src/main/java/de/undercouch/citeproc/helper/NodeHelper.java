package de.undercouch.citeproc.helper;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper methods related to XML {@link Node}s
 * @author Michel Kraemer
 */
public class NodeHelper {
    /**
     * Gets an attribute value from a node
     * @param node the node
     * @param attrName the name of the attribute
     * @return the attribute or {@code null} if the attribute does not exist
     */
    public static String getAttrValue(Node node, String attrName) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null");
        }
        if (attrName == null) {
            throw new IllegalArgumentException("Attribute name must not be null");
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return null;
        }

        Node attrNode = attributes.getNamedItem(attrName);
        if (attrNode == null) {
            return null;
        }

        return attrNode.getTextContent();
    }

    /**
     * Gets an attribute value from a node and parses it to an integer
     * @param node the node
     * @param attrName the name of the attribute
     * @param defaultValue an optional default value to return if the
     * attribute does not exist
     * @return the attribute's value or the default value
     */
    public static Integer getAttrValueInt(Node node, String attrName,
            Integer defaultValue) throws NumberFormatException {
        String s = getAttrValue(node, attrName);
        if (s == null) {
            return defaultValue;
        }
        return Integer.parseInt(s.trim());
    }

    /**
     * Iterate through the direct children of the given parent node and find
     * the child with the given name
     * @param node the parent node
     * @param name the child's name
     * @return the child or {@code null} if there is no child with the
     * given name
     */
    public static Node findDirectChild(Node node, String name) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            if (name.equals(c.getNodeName())) {
                return c;
            }
        }
        return null;
    }
}
