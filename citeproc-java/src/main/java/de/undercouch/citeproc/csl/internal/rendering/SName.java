package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A name element from a style file
 * @author Michel Kraemer
 */
public class SName implements SElement {
    private final String variable;
    private final String and;
    private final String delimiter;
    private final String delimiterPrecedesLast;
    private final String initializeWith;

    /**
     * Create the name element from an XML node
     * @param node the XML node
     * @param variable the variable that holds the name
     */
    public SName(Node node, String variable) {
        this.variable = variable;
        and = NodeHelper.getAttrValue(node, "and");

        String delimiter = NodeHelper.getAttrValue(node, "delimiter");
        if (delimiter == null) {
            delimiter = ", ";
        }
        this.delimiter = delimiter;

        String delimiterPrecedesLast = NodeHelper.getAttrValue(node,
                "delimiter-precedes-last");
        if (delimiterPrecedesLast == null) {
            delimiterPrecedesLast = "contextual";
        }
        this.delimiterPrecedesLast = delimiterPrecedesLast;

        initializeWith = NodeHelper.getAttrValue(node, "initialize-with");
    }

    @Override
    public void render(RenderContext ctx) {
        CSLName[] names = ctx.getNameVariable(variable);
        if (names == null) {
            throw new IllegalStateException("Selected names are empty");
        }

        String and;
        if ("text".equals(this.and)) {
            and = ctx.getTerm("and");
        } else if ("symbol".equals(this.and)) {
            and = "&";
        } else {
            throw new IllegalArgumentException("Unknown value for `and' " +
                    "attribute: " + this.and);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; ++i) {
            builder.append(render(names[i]));
            if (i < names.length - 1) {
                if (i == names.length - 2) {
                    switch (delimiterPrecedesLast) {
                        case "contextual":
                            if (names.length > 2) {
                                builder.append(delimiter);
                            }
                            break;

                        case "always":
                            builder.append(delimiter);
                            break;

                        default:
                            break;
                    }
                    appendAnd(builder, and);
                } else {
                    builder.append(delimiter);
                }
            }
        }
        ctx.emit(builder.toString());
    }

    private void appendAnd(StringBuilder builder, String and) {
        if (!Character.isWhitespace(builder.charAt(builder.length() - 1))) {
            builder.append(" ");
        }
        builder.append(and).append(" ");
    }

    private String render(CSLName name) {
        StringBuilder result = new StringBuilder();

        String given = name.getGiven();
        if (initializeWith != null) {
            // produce initials for each given name and append
            // 'initializeWith' to each of them
            boolean found = true;
            for (int i = 0; i < given.length(); ++i) {
                char c = given.charAt(i);
                if (Character.isWhitespace(c) || c == '.') {
                    found = true;
                } else if (found) {
                    result.append(c).append(initializeWith);
                    found = false;
                }
            }
        } else {
            result.append(given);
        }

        return result.append(name.getFamily()).toString();
    }
}