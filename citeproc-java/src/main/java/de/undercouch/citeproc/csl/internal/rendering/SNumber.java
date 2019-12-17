package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.NumberHelper;
import de.undercouch.citeproc.helper.NumberHelper.NumberToken;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A number element from a style file
 * @author Michel Kraemer
 */
public class SNumber implements SRenderingElement {
    private final String variable;
    private final Affixes affixes;
    private final TextCase textCase;

    /**
     * Creates the number element from an XML node
     * @param node the XML node
     */
    public SNumber(Node node) {
        variable = NodeHelper.getAttrValue(node, "variable");
        if (variable == null || variable.isEmpty()) {
            throw new IllegalStateException("Number element does not select a variable");
        }

        affixes = new Affixes(node);
        textCase = new TextCase(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(textCase.wrap(this::renderInternal)).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        String value = ctx.getStringVariable(variable);
        if (value != null) {
            if (!NumberHelper.isNumeric(value)) {
                ctx.emit(value);
            } else {
                List<NumberToken> tokens = NumberHelper.tokenize(value);
                for (NumberToken t : tokens) {
                    switch (t.getType()) {
                        case NUMBER:
                            ctx.emit(t.getToken());
                            break;

                        case SEPARATOR:
                            switch (t.getToken()) {
                                case "-":
                                    ctx.emit("-");
                                    break;

                                case ",":
                                    ctx.emit(", ");
                                    break;

                                case "&":
                                    ctx.emit(" & ");
                                    break;

                                default:
                                    ctx.emit(t.getToken());
                                    break;
                            }
                            break;
                    }
                }
            }
        }
    }
}
