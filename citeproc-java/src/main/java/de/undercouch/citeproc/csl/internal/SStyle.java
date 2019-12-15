package de.undercouch.citeproc.csl.internal;

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
    private final SBibliography bibliography;
    private final Map<String, SMacro> macros = new HashMap<>();

    public SStyle(Document styleDocument) {
        SBibliography bibl = null;

        Element styleRoot = styleDocument.getDocumentElement();
        NodeList styleChildren = styleRoot.getChildNodes();
        for (int i = 0; i < styleChildren.getLength(); ++i) {
            Node c = styleChildren.item(i);
            String nodeName = c.getNodeName();
            switch (nodeName) {
                case "bibliography":
                    bibl = new SBibliography(c);
                    break;
                case "macro":
                    SMacro m = new SMacro(c);
                    macros.put(m.getName(), m);
                    break;
            }
        }

        this.bibliography = bibl;
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
