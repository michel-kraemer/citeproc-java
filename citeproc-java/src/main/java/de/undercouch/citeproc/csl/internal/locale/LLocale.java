package de.undercouch.citeproc.csl.internal.locale;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * CSL localization data
 * @author Michel Kraemer
 */
public class LLocale {
    private final Locale lang;
    private final Map<LTerm.Form, Map<String, LTerm>> terms;
    private final LStyleOptions styleOptions;

    /**
     * Reads the localization data from an XML document
     * @param localeDocument the XML document
     */
    public LLocale(Document localeDocument) {
        this(localeDocument.getDocumentElement());
    }

    /**
     * Reads the localization data from an XML root node
     * @param localeRoot the root node
     */
    public LLocale(Node localeRoot) {
        String strLang = NodeHelper.getAttrValue(localeRoot, "xml:lang");
        if (strLang == null) {
            lang = null;
        } else {
            lang = Locale.forLanguageTag(strLang);
        }

        terms = new HashMap<>();
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
            styleOptions = null;
        }
    }

    private LLocale(Locale lang, Map<LTerm.Form, Map<String, LTerm>> terms, LStyleOptions styleOptions) {
        this.lang = lang;
        this.terms = terms;
        this.styleOptions = styleOptions;
    }

    /**
     * Merge this localization data with another one and return a new object
     * where the information defined in the other one overrides the information
     * imn this one.
     * @param other the other localization data
     * @return a new localization data object
     */
    public LLocale merge(LLocale other) {
        LStyleOptions styleOptions = this.styleOptions;
        if (other.styleOptions != null) {
            styleOptions = other.styleOptions;
        }

        Map<LTerm.Form, Map<String, LTerm>> terms = new HashMap<>(this.terms);
        if (other.terms != null) {
            for (Map.Entry<LTerm.Form, Map<String, LTerm>> e : other.terms.entrySet()) {
                terms.merge(e.getKey(), e.getValue(), (a, b) -> {
                    a.putAll(b);
                    return a;
                });
            }
        }

        return new LLocale(lang, terms, styleOptions);
    }

    /**
     * Get the language for which this localization data is defined
     * @return the locale
     */
    public Locale getLang() {
        return lang;
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
        if (styleOptions == null) {
            return LStyleOptions.DEFAULT;
        }
        return styleOptions;
    }
}
