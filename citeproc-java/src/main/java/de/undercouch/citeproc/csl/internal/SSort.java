package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.internal.locale.LLocale;
import de.undercouch.citeproc.helper.AlphanumComparator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public SortComparator comparator(SStyle style, LLocale locale) {
        return new SortComparator(style, locale);
    }

    /**
     * A sort comparator returned by {@link #comparator(SStyle, LLocale)}
     */
    public class SortComparator implements Comparator<CSLItemData> {
        private final SStyle style;
        private final LLocale locale;
        private final AlphanumComparator comparator;
        private int citationNumberDirection = 1;

        /**
         * Create a new sort comparator
         * @param style the current citation style
         * @param locale the current locale
         */
        public SortComparator(SStyle style, LLocale locale) {
            this.style = style;
            this.locale = locale;
            comparator = new AlphanumComparator(locale.getLang());
        }

        /**
         * Get the sort direction of the citation-number variable
         * @return the sort direction ({@code 1} for ascending and {@code -1}
         * for descending)
         */
        public int getCitationNumberDirection() {
            return citationNumberDirection;
        }

        @Override
        public int compare(CSLItemData a, CSLItemData b) {
            Integer result = null;

            for (SKey key : keys) {
                RenderContext ctxa = new RenderContext(style, locale, a);
                CollectingVariableListener vl = new CollectingVariableListener();
                ctxa.addVariableListener(vl);
                key.render(ctxa);
                ctxa.removeVariableListener(vl);
                if (vl.getCalled().contains("citation-number")) {
                    citationNumberDirection = key.getSort();
                    if (result != null) {
                        // always render all keys until we have a result and
                        // found a key with the citation-number
                        break;
                    }
                }

                if (result != null) {
                    // We already have a result. No need to render the rest.
                    continue;
                }

                RenderContext ctxb = new RenderContext(style, locale, b);
                key.render(ctxb);

                String sa = ctxa.getResult().toString();
                String sb = ctxb.getResult().toString();

                // empty elements should be put at the end of the list
                if (sa.isEmpty() && !sb.isEmpty()) {
                    result = 1;
                } else if (!sa.isEmpty() && sb.isEmpty()) {
                    result = -1;
                } else {
                    int c = comparator.compare(sa, sb);
                    if (c != 0) {
                        result = c * key.getSort();
                    }
                }
            }

            return result != null ? result : 0;
        }
    }
}
