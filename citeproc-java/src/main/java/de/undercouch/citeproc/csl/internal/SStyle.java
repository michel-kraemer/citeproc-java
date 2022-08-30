package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.locale.LLocale;
import de.undercouch.citeproc.csl.internal.rendering.SNameInheritableAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A citation style
 * @author Michel Kraemer
 */
public class SStyle {
    private final String defaultLocale;
    private final List<LLocale> locales;
    private final SCitation citation;
    private final SBibliography bibliography;
    private final Map<String, SMacro> macros = new HashMap<>();
    private final SNameInheritableAttributes inheritableNameAttributes;
    private final String pageRangeFormat;

    /**
     * Creates the citation style from an XML document
     * @param styleDocument the XML document
     */
    public SStyle(Document styleDocument) {
        List<LLocale> locales = new ArrayList<>();
        SCitation citation = null;
        SBibliography bibl = null;

        Element styleRoot = styleDocument.getDocumentElement();
        NodeList styleChildren = styleRoot.getChildNodes();
        for (int i = 0; i < styleChildren.getLength(); ++i) {
            Node c = styleChildren.item(i);
            String nodeName = c.getNodeName();
            switch (nodeName) {
                case "locale":
                    locales.add(new LLocale(c));
                    break;
                case "citation":
                    citation = new SCitation(c);
                    break;
                case "bibliography":
                    bibl = new SBibliography(c);
                    break;
                case "macro":
                    SMacro m = new SMacro(c);
                    if (m.getName() != null && !m.getName().isEmpty()) {
                        macros.put(m.getName(), m);
                    }
                    break;
            }
        }

        this.locales = locales;
        this.citation = citation;
        this.bibliography = bibl;
        this.inheritableNameAttributes = new SNameInheritableAttributes(styleRoot);
        this.pageRangeFormat = NodeHelper.getAttrValue(styleRoot, "page-range-format");
        this.defaultLocale = NodeHelper.getAttrValue(styleRoot, "default-locale");
    }

    /**
     * Get the default locale for this style if defined
     * @return the default locale or {@code null} if no default locale is
     * defined in this style
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Get additional localization data defined in the style file. This
     * data may override or augments information from the locale file
     * @return the additional localization data (or an empty list if there
     * is no additional information in the style file)
     */
    public List<LLocale> getLocales() {
        return locales;
    }

    /**
     * Get the citation element defined in the style file
     * @return the citation element or {@code null} if the style file does not
     * have one
     */
    public SCitation getCitation() {
        return citation;
    }

    /**
     * Get the bibliography element defined in the style file
     * @return the bibliography element or {@code null} if the style file
     * does not have one
     */
    public SBibliography getBibliography() {
        return bibliography;
    }

    /**
     * Get the macros defined in the citation style file
     * @return the macros
     */
    public Map<String, SMacro> getMacros() {
        return macros;
    }

    /**
     * Get attributes that can be inherited to name elements
     * @return the name attributes
     */
    public SNameInheritableAttributes getInheritableNameAttributes() {
        return inheritableNameAttributes;
    }

    /**
     * Get the page range format defined for this style
     * @return the format or {@code null} if no value has been defined
     */
    public String getPageRangeFormat() {
        return pageRangeFormat;
    }
}
