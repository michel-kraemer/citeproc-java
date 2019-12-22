package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.internal.locale.LLocale;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A sort element inside a bibliography or citation element
 * @author Michel Kraemer
 */
public class SSort {
    private final List<SKey> keys = new ArrayList<>();

    /**
     * Creates the sort element from an XML node
     * @param node the XML node
     */
    public SSort(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            String nodeName = c.getNodeName();
            if ("key".equals(nodeName)) {
                keys.add(new SKey(c));
            }
        }
    }

    /**
     * Creates a comparator that is able to compare {@link CSLItemData} objects
     * according
     * @param style the current citation style
     * @param locale the current locale
     * @return the comparator
     */
    public Comparator<CSLItemData> comparator(SStyle style, LLocale locale) {
        Collator collator = Collator.getInstance(locale.getLang());
        return (a, b) -> {
            RenderContext ctxa = new RenderContext(style, locale, a);
            RenderContext ctxb = new RenderContext(style, locale, b);
            for (SKey key : keys) {
                key.render(ctxa);
                key.render(ctxb);

                int c = collator.compare(ctxa.getResult().toString(),
                        ctxb.getResult().toString());
                if (c != 0) {
                    return c * key.getSort();
                }
            }
            return 0;
        };
    }
}
