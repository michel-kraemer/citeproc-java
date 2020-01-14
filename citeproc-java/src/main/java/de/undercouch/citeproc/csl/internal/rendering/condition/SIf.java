package de.undercouch.citeproc.csl.internal.rendering.condition;

import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.helper.NumberElement;
import de.undercouch.citeproc.csl.internal.helper.NumberParser;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.NumberHelper;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A conditional element in a style file
 * @author Michel Kraemer
 */
public class SIf extends SCondition {
    private static int ALL = 0;
    private static int ANY = 1;
    private static int NONE = 2;

    private final String[] types;
    private final String[] variables;
    private final String[] isNumerics;
    private final String[] numbers;
    private final int match;

    /**
     * Create the conditional element from an XML node
     * @param node the XML node
     */
    public SIf(Node node) {
        super(node);

        // get the citation item types to check against
        String type = NodeHelper.getAttrValue(node, "type");
        if (type != null) {
            types = type.split("\\s+");
        } else {
            types = null;
        }

        // get the variables to check
        String variable = NodeHelper.getAttrValue(node, "variable");
        if (variable != null) {
            variables = variable.split("\\s+");
        } else {
            variables = null;
        }

        // get the numeric variables to check
        String isNumeric = NodeHelper.getAttrValue(node, "is-numeric");
        if (isNumeric != null) {
            isNumerics = isNumeric.split("\\s+");
        } else {
            isNumerics = null;
        }

        // get the labels to check the number variable against
        String number = NodeHelper.getAttrValue(node, "number");
        if (number != null) {
            numbers = number.split("\\s+");
        } else {
            numbers = null;
        }

        // get the match mode
        String match = NodeHelper.getAttrValue(node, "match");
        if (match == null || match.equals("all")) {
            this.match = ALL;
        } else if (match.equals("any")) {
            this.match = ANY;
        } else if (match.equals("none")) {
            this.match = NONE;
        } else {
            throw new IllegalStateException("Unknown match mode: " + match);
        }
    }

    @Override
    public boolean matches(RenderContext ctx) {
        if (types == null && variables == null && isNumerics == null && numbers == null) {
            return false;
        }

        CSLType cslType = ctx.getItemData().getType();
        if (types != null && cslType != null) {
            // check if the citation item matches the given types
            String type = cslType.toString();
            for (String s : types) {
                boolean r = type.equals(s);
                if (match == ALL && !r) {
                    return false;
                }
                if (match == ANY && r) {
                    return true;
                }
                if (match == NONE && r) {
                    return false;
                }
            }
        }

        if (variables != null) {
            // check the values of the given variables
            for (String v : variables) {
                Object o = ctx.getVariable(v);
                if (match == ALL && o == null) {
                    return false;
                }
                if (match == ANY && o != null) {
                    return true;
                }
                if (match == NONE && o != null) {
                    return false;
                }
            }
        }

        if (isNumerics != null) {
            // check if the given variables are numeric
            for (String v : isNumerics) {
                Object o = ctx.getVariable(v);
                boolean numeric = o != null && (o instanceof Number ||
                        NumberHelper.isNumeric(String.valueOf(o)));
                if (match == ALL && !numeric) {
                    return false;
                }
                if (match == ANY && numeric) {
                    return true;
                }
                if (match == NONE && numeric) {
                    return false;
                }
            }
        }

        if (numbers != null) {
            // check if the number variable has the given label(s)
            String v = ctx.getStringVariable("number");
            String firstLabel = null;
            if (v != null) {
                List<NumberElement> elements = NumberParser.parse(v);
                if (elements.get(0).getLabel() != null) {
                    firstLabel = elements.get(0).getLabel().toString();
                }
            }
            if (firstLabel == null) {
                firstLabel = "number";
            }

            for (String number : numbers) {
                if (match == ALL && !number.equals(firstLabel)) {
                    return false;
                }
                if (match == ANY && number.equals(firstLabel)) {
                    return true;
                }
                if (match == NONE && number.equals(firstLabel)) {
                    return false;
                }
            }
        }

        return match != ANY;
    }
}
