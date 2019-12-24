package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.locale.LLocale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * A citation style
 * @author Michel Kraemer
 */
public class SStyle {
    private final LLocale locale;
    private final SCitation citation;
    private final SBibliography bibliography;
    private final Map<String, SMacro> macros = new HashMap<>();

    /**
     * Creates the citation style from an XML document
     * @param styleDocument the XML document
     */
    public SStyle(Document styleDocument) {
        LLocale locale = null;
        SCitation citation = null;
        SBibliography bibl = null;

        Element styleRoot = styleDocument.getDocumentElement();
        NodeList styleChildren = styleRoot.getChildNodes();
        for (int i = 0; i < styleChildren.getLength(); ++i) {
            Node c = styleChildren.item(i);
            String nodeName = c.getNodeName();
            switch (nodeName) {
                case "locale":
                    locale = new LLocale(c);
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

        this.locale = locale;
        this.citation = citation;
        this.bibliography = bibl;
    }

    /**
     * Get additional localization data defined in the style file. This
     * data may override or augments information from the locale file
     * @return the additional localization data (or {@code null} if there
     * is no additional information in the style file)
     */
    public LLocale getLocale() {
        return locale;
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
}
