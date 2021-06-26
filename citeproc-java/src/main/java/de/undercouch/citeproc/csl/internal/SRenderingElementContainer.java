package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.rendering.SChoose;
import de.undercouch.citeproc.csl.internal.rendering.SDate;
import de.undercouch.citeproc.csl.internal.rendering.SGroup;
import de.undercouch.citeproc.csl.internal.rendering.SLabel;
import de.undercouch.citeproc.csl.internal.rendering.SNames;
import de.undercouch.citeproc.csl.internal.rendering.SNumber;
import de.undercouch.citeproc.csl.internal.rendering.SRenderingElement;
import de.undercouch.citeproc.csl.internal.rendering.SText;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container for style elements
 * @author Michel Kraemer
 */
public class SRenderingElementContainer {
    private final List<Object> rawElements = new ArrayList<>();

    /**
     * Construct the container from an XML node
     * @param node the XML node
     */
    public SRenderingElementContainer(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            String nodeName = c.getNodeName();
            Object element = null;
            if ("choose".equals(nodeName)) {
                element = new SChoose(c);
            } else if ("date".equals(nodeName)) {
                element = new SDate(c);
            } else if ("group".equals(nodeName)) {
                element = new SGroup(c);
            } else if ("label".equals(nodeName)) {
                element = new SLabel(c);
            } else if ("names".equals(nodeName)) {
                element = new SNames(c);
            } else if ("number".equals(nodeName)) {
                element = new SNumber(c);
            } else if ("text".equals(nodeName)) {
                element = new SText(c);
            }
            if (element != null) {
                rawElements.add(element);
            }
        }
    }

    public List<SRenderingElement> getElements(RenderContext ctx) {
        if (rawElements.isEmpty()) {
            return Collections.emptyList();
        }

        List<SRenderingElement> result = new ArrayList<>();
        for (Object o : rawElements) {
            if (o instanceof SRenderingElement) {
                result.add((SRenderingElement)o);
            } else if (o instanceof SChoose) {
                SChoose choose = (SChoose)o;
                result.addAll(choose.evaluate(ctx));
            } else {
                throw new RuntimeException("Unknown raw element type: " + o.getClass());
            }
        }
        return result;
    }
}
