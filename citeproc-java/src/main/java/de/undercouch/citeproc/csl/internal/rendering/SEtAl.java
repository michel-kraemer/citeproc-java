package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.SUFFIX;

/**
 * An et-al element from a style file
 * @author Michel Kraemer
 */
public class SEtAl implements SRenderingElement {
    private final int formattingAttributes;
    private final String term;

    /**
     * Construct the et-al element from an XML node
     * @param node the XML node
     */
    public SEtAl(Node node) {
        String term = null;
        if (node != null) {
            term = NodeHelper.getAttrValue(node, "term");
        }
        if (term == null) {
            term = "et-al";
        }
        this.term = term;

        int formattingAttributes = 0;
        if (node != null) {
            formattingAttributes = FormattingAttributes.of(node);
        }
        this.formattingAttributes = formattingAttributes;
    }

    @Override
    public void render(RenderContext ctx) {
        String etAl = ctx.getTerm(term);
        if (etAl != null) {
            if (formattingAttributes == 0) {
                ctx.emit(" " + etAl, SUFFIX);
            } else {
                ctx.emit(" ", SUFFIX);
                ctx.emit(etAl, SUFFIX, formattingAttributes);
            }
        }
    }
}
