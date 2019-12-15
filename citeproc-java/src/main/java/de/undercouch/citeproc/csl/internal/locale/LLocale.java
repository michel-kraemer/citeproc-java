package de.undercouch.citeproc.csl.internal.locale;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * CSL localization data
 * @author Michel Kraemer
 */
public class LLocale {
    private final Map<LTerm.Form, Map<String, LTerm>> terms = new HashMap<>();
    private final LStyleOptions styleOptions;

    /**
     * Reads the localization data from an XML document
     * @param localeDocument the XML document
     */
    public LLocale(Document localeDocument) {
        Element localeRoot = localeDocument.getDocumentElement();

        Node termsNode = NodeHelper.findDirectChild(localeRoot, "terms");
        if (termsNode != null) {
            NodeList termsNodeChildren = termsNode.getChildNodes();
            for (int j = 0; j < termsNodeChildren.getLength(); ++j) {
                Node c = termsNodeChildren.item(j);
                if ("term".equals(c.getNodeName())) {
                    LTerm t = new LTerm(c);
                    Map<String, LTerm> m = terms.computeIfAbsent(t.getForm(),
                            k -> new HashMap<>());
                    m.put(t.getName(), t);
                }
            }
        }

        Node styleOptionsNode = NodeHelper.findDirectChild(localeRoot, "style-options");
        if (styleOptionsNode != null) {
            styleOptions = new LStyleOptions(styleOptionsNode);
        } else {
            styleOptions = new LStyleOptions();
        }
    }

    /**
     * Get the terms defined in the localization data
     * @return the terms
     */
    public Map<LTerm.Form, Map<String, LTerm>> getTerms() {
        return terms;
    }

    /**
     * Get the global style rendering options defined in the localization data
     * @return the style options
     */
    public LStyleOptions getStyleOptions() {
        return styleOptions;
    }
}
