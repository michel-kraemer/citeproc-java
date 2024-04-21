package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.rendering.condition.SCondition;
import de.undercouch.citeproc.csl.internal.rendering.condition.SElse;
import de.undercouch.citeproc.csl.internal.rendering.condition.SIf;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * An element from a style file that conditionally "chooses" which of its
 * children to render
 * @author Michel Kraemer
 */
public class SChoose {
    private final List<SCondition> conditions = new ArrayList<>();

    /**
     * Create the element from an XML node
     * @param node the XML node
     */
    public SChoose(Node node) {
        NodeList children = node.getChildNodes();

        boolean done = false;
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            if (c.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String nodeName = c.getNodeName();
            SCondition condition;
            switch (nodeName) {
                case "if":
                    if (!conditions.isEmpty()) {
                        throw new IllegalStateException("Multiple `if' nodes found");
                    }
                    condition = new SIf(c);
                    break;

                case "else-if":
                    if (conditions.isEmpty()) {
                        throw new IllegalStateException("`else-if' without `if' found");
                    }
                    if (done) {
                        throw new IllegalStateException("`else-if' following `else' found");
                    }
                    condition = new SIf(c);
                    break;

                case "else":
                    if (done) {
                        throw new IllegalStateException("Multiple `else' nodes found");
                    }
                    condition = new SElse(c);
                    done = true;
                    break;

                default:
                    throw new IllegalStateException("Unknown conditional element: " + nodeName);
            }

            conditions.add(condition);
        }
    }

    public List<SRenderingElement> evaluate(RenderContext ctx) {
        List<SRenderingElement> result = new ArrayList<>();
        for (SCondition c : conditions) {
            if (c.matches(ctx)) {
                result.addAll(c.getElements(ctx));
                break;
            }
        }
        return result;
    }
}
