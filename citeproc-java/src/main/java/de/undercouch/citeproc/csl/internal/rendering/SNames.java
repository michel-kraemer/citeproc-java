package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
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
    private final Affixes affixes;
    private final SName name;
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
     * @param variable the name variable to select
     * @param parseSubstitute {@code true} if the substitute child element
     * should be parsed, {@code false} if it should be ignored
     */
    public SNames(Node node, String variable, boolean parseSubstitute) {
        SName name = null;
        SSubstitute substitute = null;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            String nodeName = c.getNodeName();
            SElement element = null;
            if ("name".equals(nodeName)) {
                name = new SName(c, variable);
                element = name;
            } else if ("label".equals(nodeName)) {
                element = new SLabel(c, variable);
            } else if (parseSubstitute && "substitute".equals(nodeName)) {
                substitute = new SSubstitute(c, node);
            }
            if (element != null) {
                elements.add(element);
            }
        }

        if (name == null) {
            name = new SName(null, variable);
            elements.add(0, name);
        }

        this.name = name;
        this.substitute = substitute;

        if (name.getForm() == SName.FORM_COUNT) {
            affixes = new Affixes();
        } else {
            affixes = new Affixes(node);
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
        affixes.wrap(this::renderInternal).accept(tmp);

        if (tmp.getResult().isEmpty()) {
            if (substitute != null) {
                substitute.render(tmp);
            }
        }

        ctx.emit(tmp.getResult());
    }

    private void renderInternal(RenderContext ctx) {
        for (SElement e : elements) {
            e.render(ctx);
        }
    }
}
