package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.Quotes;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.csl.internal.locale.Term;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A text element from a style file
 * @author Michel Kraemer
 */
public class SText implements SRenderingElement {
    private final String variable;
    private final String macro;
    private final String term;
    private final String form;
    private final String value;
    private final Affixes affixes;
    private final Quotes quotes;
    private final TextCase textCase;

    /**
     * Creates the text element from an XML node
     * @param node the XML node
     */
    public SText(Node node) {
        variable = NodeHelper.getAttrValue(node, "variable");
        macro = NodeHelper.getAttrValue(node, "macro");
        term = NodeHelper.getAttrValue(node, "term");
        form = NodeHelper.getAttrValue(node, "form");
        value = NodeHelper.getAttrValue(node, "value");
        affixes = new Affixes(node);
        quotes = new Quotes(node);
        textCase = new TextCase(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(quotes.wrap(textCase.wrap(this::renderInternal))).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        if (variable != null && !variable.isEmpty()) {
            String v = ctx.getVariable(variable);
            if (v != null) {
                if (variable.equals("page")) {
                    String delimiter = ctx.getTerm("page-range-delimiter");
                    v = v.replace("-", delimiter);
                }
                ctx.emit(v);
            }
        } else if (macro != null && !macro.isEmpty()) {
            ctx.getMacro(macro).render(ctx);
        } else if (term != null && !term.isEmpty()) {
            String f = form;
            if (f == null) {
                f = "long";
            }
            ctx.emit(ctx.getTerm(term, Term.Form.fromString(f)));
        } else if (value != null) {
            ctx.emit(value);
        } else {
            throw new IllegalStateException("Text element does not select a " +
                    "variable, term, value, or macro");
        }
    }
}
