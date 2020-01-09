package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.bibtex.PageParser;
import de.undercouch.citeproc.bibtex.PageRange;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.StripPeriods;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * A label element from a style file
 * @author Michel Kraemer
 */
public class SLabel implements SRenderingElement {
    private final String variable;
    private final String form;
    private final Affixes affixes;
    private final TextCase textCase;
    private final StripPeriods stripPeriods;

    /**
     * Creates the label element from an XML node
     * @param node the XML node
     */
    public SLabel(Node node) {
        this(node, NodeHelper.getAttrValue(node, "variable"));
    }

    /**
     * Creates the label element from an XML node but with a different variable
     * @param node the XML node
     * @param variable the variable
     */
    public SLabel(Node node, String variable) {
        this.variable = variable;

        String form = NodeHelper.getAttrValue(node, "form");
        if (form == null) {
            form = "long";
        }
        this.form = form;

        affixes = new Affixes(node);
        textCase = new TextCase(node);
        stripPeriods = new StripPeriods(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(textCase.wrap(stripPeriods.wrap(this::renderInternal))).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        if (variable == null || variable.isEmpty()) {
            return;
        }

        Object value = ctx.getVariable(variable);
        if (value == null) {
            return;
        }

        boolean plural = false;
        if (variable.equals("page")) {
            PageRange range = PageParser.parse(String.valueOf(value));
            plural = range.isMultiplePages();
        }

        ctx.emit(ctx.getTerm(variable, LTerm.Form.fromString(form), plural));
    }
}
