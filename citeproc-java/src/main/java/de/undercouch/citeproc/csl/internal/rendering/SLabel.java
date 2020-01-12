package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.bibtex.PageParser;
import de.undercouch.citeproc.bibtex.PageRange;
import de.undercouch.citeproc.csl.CSLLabel;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.StripPeriods;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.csl.internal.helper.NumberElement;
import de.undercouch.citeproc.csl.internal.helper.NumberParser;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.List;

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
        render(ctx, 0);
    }

    /**
     * Renders this label but selects a different number element if
     * {@link #variable} equals {@code "number"} and has been parsed
     * @param ctx the context in which to render
     * @param nNumberElement the index of the number element to select
     */
    public void render(RenderContext ctx, int nNumberElement) {
        affixes.wrap(textCase.wrap(stripPeriods.wrap(ctx2 ->
                renderInternal(ctx2, nNumberElement)))).accept(ctx);
    }

    private void renderInternal(RenderContext ctx, int nNumberElement) {
        if (variable == null || variable.isEmpty()) {
            return;
        }

        Object value = ctx.getVariable(variable);
        if (value == null) {
            return;
        }

        String term = variable;
        boolean plural = false;
        boolean isLocator = false;
        if (variable.equals("page")) {
            PageRange range = PageParser.parse(String.valueOf(value));
            plural = range.isMultiplePages();
        } else if (variable.equals("number") || (isLocator = variable.equals("locator"))) {
            List<NumberElement> elements = NumberParser.parse(String.valueOf(value));
            if (elements.size() > nNumberElement) {
                NumberElement element = elements.get(nNumberElement);
                if (element != null) {
                    if (element.getLabel() != null) {
                        term = element.getLabel().toString();
                    } else if (nNumberElement == 0 && isLocator) {
                        CSLLabel label = null;
                        if (ctx.getCitationItem() != null) {
                            label = ctx.getCitationItem().getLabel();
                        }
                        if (label != null) {
                            term = label.toString();
                        } else {
                            term = "page";
                        }
                    }
                    plural = element.isPlural();
                }
            }
        }

        ctx.emit(ctx.getTerm(term, LTerm.Form.fromString(form), plural));
    }
}
