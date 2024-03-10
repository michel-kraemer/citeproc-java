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
    private final Map<String, LDate> dateFormats;
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

        dateFormats = new HashMap<>();
        terms = new HashMap<>();
        LStyleOptions styleOptions = null;

        NodeList children = localeRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            switch (c.getNodeName()) {
                case "terms":
                    NodeList termsNodeChildren = c.getChildNodes();
                    for (int j = 0; j < termsNodeChildren.getLength(); ++j) {
                        Node tc = termsNodeChildren.item(j);
                        if ("term".equals(tc.getNodeName())) {
                            LTerm t = new LTerm(tc);
                            Map<String, LTerm> m = terms.computeIfAbsent(t.getForm(),
                                    k -> new HashMap<>());
                            m.put(t.getName(), t);
                        }
                    }
                    break;

                case "style-options":
                    styleOptions = new LStyleOptions(c);
                    break;

                case "date":
                    LDate d = new LDate(c);
                    dateFormats.put(d.getForm(), d);
                    break;
            }
        }

        this.styleOptions = styleOptions;
    }

    private LLocale(Locale lang, Map<String, LDate> dateFormats,
            Map<LTerm.Form, Map<String, LTerm>> terms, LStyleOptions styleOptions) {
        this.lang = lang;
        this.dateFormats = dateFormats;
        this.terms = terms;
        this.styleOptions = styleOptions;
    }

    /**
     * Merge this localization data with another one and return a new object
     * where the information defined in the other one overrides the information
     * in this one.
     * @param other the other localization data
     * @return a new localization data object
     */
    public LLocale merge(LLocale other) {
        LStyleOptions styleOptions = this.styleOptions;
        if (other.styleOptions != null) {
            styleOptions = other.styleOptions;
        }

        Map<String, LDate> dateFormats = new HashMap<>(this.dateFormats);
        if (other.dateFormats != null) {
            dateFormats.putAll(other.dateFormats);
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

        return new LLocale(lang, dateFormats, terms, styleOptions);
    }

    /**
     * Get the language for which this localization data is defined
     * @return the locale
     */
    public Locale getLang() {
        return lang;
    }

    /**
     * Get the date formats defined in the localization data
     * @return the date formats
     */
    public Map<String, LDate> getDateFormats() {
        return dateFormats;
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
