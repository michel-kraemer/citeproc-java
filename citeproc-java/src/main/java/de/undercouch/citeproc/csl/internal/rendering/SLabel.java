package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.StripPeriods;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.NumberHelper;
import de.undercouch.citeproc.helper.NumberHelper.NumberToken;
import de.undercouch.citeproc.helper.NumberHelper.NumberTokenType;
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
        variable = NodeHelper.getAttrValue(node, "variable");
        if (variable == null || variable.isEmpty()) {
            throw new IllegalStateException("Label element does not select a variable");
        }

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
        String value = ctx.getVariable(variable);
        if (value == null) {
            return;
        }

        boolean plural = false;
        if (variable.equals("page") && NumberHelper.isNumeric(value)) {
            List<NumberToken> tokens = NumberHelper.tokenize(value);
            long numbers = tokens.stream()
                    .filter(t -> t.getType() == NumberTokenType.NUMBER)
                    .count();
            plural = numbers > 1;
        }

        ctx.emit(ctx.getTerm(variable, LTerm.Form.fromString(form), plural));
    }
}
