package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * A names element from a style file
 * @author Michel Kraemer
 */
public class SNames implements SRenderingElement {
    private final String variableAttribute;
    private final Affixes affixes;
    private final int formattingAttributes;
    private final SSubstitute substitute;
    private final List<SElement> elements = new ArrayList<>();

    /**
     * Creates the names element from an XML node
     * @param node the XML node
     */
    public SNames(Node node) {
        this(node, NodeHelper.getAttrValue(node, "variable"), true);
    }

    /**
     * Creates the names element from an XML node but with a different variable
     * @param node the XML node
     * @param variableAttribute the value of the variable attribute
     * @param parseSubstitute {@code true} if the substitute child element
     * should be parsed, {@code false} if it should be ignored
     */
    public SNames(Node node, String variableAttribute, boolean parseSubstitute) {
        String[] variables;
        if (variableAttribute == null) {
            variables = new String[0];
        } else {
            variables = variableAttribute.split("\\s+");
        }

        String delimiter = NodeHelper.getAttrValue(node, "delimiter");
        this.formattingAttributes = FormattingAttributes.of(node);

        boolean allFormCount = true;
        Node firstNameNode = null;
        for (String variable : variables) {
            if (!elements.isEmpty() && delimiter != null) {
                elements.add((SRenderingElement)ctx -> {
                    if (!ctx.getResult().isEmpty()) {
                        ctx.emit(delimiter);
                    }
                });
            }

            List<SElement> elementsForVariable = new ArrayList<>();
            SName name = null;
            Node nameNode = null;
            SLabel label = null;
            int namePos = -1;
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                Node c = children.item(i);
                String nodeName = c.getNodeName();
                if ("name".equals(nodeName)) {
                    name = new SName(c, variable);
                    nameNode = c;
                    if (namePos >= 0) {
                        elementsForVariable.set(namePos, name);
                    } else {
                        namePos = elementsForVariable.size();
                        elementsForVariable.add(name);
                    }
                } else if (label == null && "label".equals(nodeName)) {
                    label = new SLabel(c, variable);
                    elementsForVariable.add(label);
                }
            }

            if (name == null) {
                name = new SName(null, variable);
                elementsForVariable.add(0, name);
            }
            if (firstNameNode == null) {
                firstNameNode = nameNode;
            }

            allFormCount = allFormCount && name.getForm() == SName.FORM_COUNT;

            elements.addAll(elementsForVariable);
        }

        this.variableAttribute = variableAttribute;

        if (parseSubstitute) {
            Node substituteNode = NodeHelper.findDirectChild(node, "substitute");
            if (substituteNode != null) {
                substitute = new SSubstitute(substituteNode, node);
            } else {
                substitute = null;
            }
        } else {
            substitute = null;
        }

        if (allFormCount) {
            elements.clear();
            elements.add(new SName(firstNameNode, variableAttribute));
            affixes = new Affixes();
        } else {
            affixes = new Affixes(node);
        }
    }

    /**
     * Get the value of the {@code variable} attribute
     * @return the attribute value
     */
    public String getVariableAttribute() {
        return variableAttribute;
    }

    @Override
    public void render(RenderContext ctx) {
        RenderContext tmp = new RenderContext(ctx);
        affixes.wrap(this::renderInternal).accept(tmp);

        if (tmp.getResult().isEmpty()) {
            if (substitute != null) {
                substitute.render(tmp);
            }
        }

        ctx.emit(tmp.getResult());
    }

    private void renderInternal(RenderContext ctx) {
        RenderContext child = new RenderContext(ctx);
        for (SElement e : elements) {
            e.render(child);
        }
        ctx.emit(child.getResult(), formattingAttributes);
    }
}
