package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A names element from a style file
 * @author Michel Kraemer
 */
public class SNames implements SRenderingElement {
    private final SName name;
    private final Affixes affixes;
    private final SLabel label;
    private final SSubstitute substitute;

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
     * @param variable the name variable to select
     * @param parseSubstitute {@code true} if the substitute child element
     * should be parsed, {@code false} if it should be ignored
     */
    public SNames(Node node, String variable, boolean parseSubstitute) {
        name = new SName(NodeHelper.findDirectChild(node, "name"), variable);
        if (name.getForm() == SName.FORM_COUNT) {
            affixes = new Affixes();
        } else {
            affixes = new Affixes(node);
        }

        Node labelNode = NodeHelper.findDirectChild(node, "label");
        if (labelNode != null) {
            label = new SLabel(labelNode, variable);
        } else {
            label = null;
        }

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
    }

    /**
     * Get the name variable this element selects
     * @return the name variable
     */
    public String getVariable() {
        return name.getVariable();
    }

    @Override
    public void render(RenderContext ctx) {
        RenderContext tmp = new RenderContext(ctx);
        if (label != null) {
            label.render(tmp);
        }

        affixes.wrap(name::render).accept(tmp);

        if (tmp.getResult().isEmpty()) {
            if (substitute != null) {
                substitute.render(tmp);
            }
        }

        ctx.emit(tmp.getResult());
    }
}
