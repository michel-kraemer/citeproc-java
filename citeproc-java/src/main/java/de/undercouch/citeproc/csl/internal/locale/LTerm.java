package de.undercouch.citeproc.csl.internal.locale;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;

/**
 * A term defined in a local file
 * @author Michel Kraemer
 */
public class LTerm {
    private final String name;
    private final Form form;
    private final String single;
    private final String multiple;

    /**
     * The form in which the term was defined
     */
    public enum Form {
        /**
         * A long term
         */
        LONG,

        /**
         * A short term
         */
        SHORT,

        /**
         * A verb
         */
        VERB,

        /**
         * The short form of a verb
         */
        VERB_SHORT,

        /**
         * A symbol
         */
        SYMBOL;

        /**
         * Parse the term form from a string
         * @param str the string to parse
         * @return the term form
         * @throws IllegalArgumentException if the string contains an unknown form
         */
        public static Form fromString(String str) {
            switch (str) {
                case "long":
                    return LONG;
                case "short":
                    return SHORT;
                case "verb":
                    return VERB;
                case "verb-short":
                    return VERB_SHORT;
                case "symbol":
                    return SYMBOL;
                default:
                    throw new IllegalArgumentException("Unknown term form: " + str);
            }
        }
    }

    /**
     * Parses a term from an XML node
     * @param node the XML node
     */
    public LTerm(Node node) {
        name = NodeHelper.getAttrValue(node, "name");
        String strForm = NodeHelper.getAttrValue(node, "form");
        if (strForm == null) {
            strForm = "long";
        }
        this.form = Form.fromString(strForm);

        String single = null;
        String multiple = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String nodeName = c.getNodeName();
            switch (nodeName) {
                case "single":
                    single = c.getTextContent();
                    break;
                case "multiple":
                    multiple = c.getTextContent();
                    break;
                default:
                    break;
            }
        }

        if (single == null) {
            single = node.getTextContent();
            if (single == null) {
                throw new IllegalStateException("Invalid term value");
            }
        }

        if (multiple == null) {
            multiple = single;
        }

        this.single = single;
        this.multiple = multiple;
    }

    /**
     * Get the term's name
     * @return the term's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the term's form
     * @return the term's form
     */
    public Form getForm() {
        return form;
    }

    /**
     * Get the singular form of the term
     * @return the singular form
     */
    public String getSingle() {
        return single;
    }

    /**
     * Get the plural form of the term (might be the same as the singular form
     * if no plural form was defined)
     * @return the plural form
     */
    public String getMultiple() {
        return multiple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LTerm term = (LTerm)o;
        return name.equals(term.name) &&
                form.equals(term.form) &&
                single.equals(term.single) &&
                multiple.equals(term.multiple);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, form, single, multiple);
    }
}
