package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A name element from a style file
 * @author Michel Kraemer
 */
public class SName implements SElement {
    private final String variable;
    private final String and;
    private final String initializeWith;

    /**
     * Create the name element from an XML node
     * @param node the XML node
     * @param variable the variable that holds the name
     */
    public SName(Node node, String variable) {
        this.variable = variable;
        and = NodeHelper.getAttrValue(node, "and");
        initializeWith = NodeHelper.getAttrValue(node, "initialize-with");
    }

    @Override
    public void render(RenderContext ctx) {
        CSLName[] names = ctx.getNameVariable(variable);
        if (names == null) {
            throw new IllegalStateException("Selected names are empty");
        }

        String delimiter;
        if ("text".equals(and)) {
            delimiter = ctx.getTerm("and");
        } else if ("symbol".equals(and)) {
            delimiter = "&";
        } else {
            throw new IllegalArgumentException("Unknown value for `and' " +
                    "attribute: " + and);
        }
        delimiter = " " + delimiter + " ";

        String result = Arrays.stream(names)
                .map(this::render)
                .collect(Collectors.joining(delimiter));
        ctx.emit(result);
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
                if (Character.isWhitespace(c)) {
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
