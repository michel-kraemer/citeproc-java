package de.undercouch.citeproc.csl.internal.rendering.condition;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A conditional element in a style file
 * @author Michel Kraemer
 */
public class SIf extends SCondition {
    private final String[] types;
    private final String[] variables;
    private final String match;

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

        if (types == null) {
            // get the variables to check
            String variable = NodeHelper.getAttrValue(node, "variable");
            if (variable != null) {
                variables = variable.split("\\s+");
            } else {
                variables = null;
            }
        } else {
            variables = null;
        }

        // get the match mode
        match = NodeHelper.getAttrValue(node, "match");
        if (match != null && !"none".equals(match) && !"any".equals(match) &&
                !"all".equals(match)) {
            throw new IllegalStateException("Unknown match mode: " + match);
        }
    }

    @Override
    public boolean matches(RenderContext ctx) {
        boolean result = false;

        if (types != null) {
            // check of the citation item matches the given types
            String type = ctx.getItemData().getType().toString();
            Stream<String> s = Arrays.stream(types);
            if ("none".equals(match)) {
                result = s.noneMatch(type::equals);
            } else if ("any".equals(match)) {
                result = s.anyMatch(type::equals);
            } else {
                result = s.allMatch(type::equals);
            }
        } else if (variables != null) {
            // check the values of the given variables
            Stream<String> s = Arrays.stream(variables)
                    .map(ctx::getVariable);
            if ("none".equals(match)) {
                result = s.noneMatch(Objects::nonNull);
            } else if ("any".equals(match)) {
                result = s.anyMatch(Objects::nonNull);
            } else {
                result = s.allMatch(Objects::nonNull);
            }
        }

        return result;
    }
}
