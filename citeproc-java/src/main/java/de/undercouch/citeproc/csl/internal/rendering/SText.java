package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SMacro;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.VariableForm;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.csl.internal.behavior.Quotes;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.csl.internal.helper.NumberElement;
import de.undercouch.citeproc.csl.internal.helper.NumberParser;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.List;

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
    private final int formattingAttributes;

    /**
     * Creates the text element from an XML node
     * @param node the XML node
     */
    public SText(Node node) {
        variable = NodeHelper.getAttrValue(node, "variable");
        macro = NodeHelper.getAttrValue(node, "macro");
        term = NodeHelper.getAttrValue(node, "term");
        value = NodeHelper.getAttrValue(node, "value");
        affixes = new Affixes(node);
        quotes = new Quotes(node);
        textCase = new TextCase(node);
        formattingAttributes = FormattingAttributes.of(node);

        String form = NodeHelper.getAttrValue(node, "form");
        if (form == null) {
            form = "long";
        }
        this.form = form;
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(quotes.wrap(textCase.wrap(this::renderInternal))).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        if (variable != null && !variable.isEmpty()) {
            // year-suffix is a special variable that is used to disambiguate
            // dates. Listeners should not be notified about it
            boolean ignoreListeners = variable.equals("year-suffix");

            String v = ctx.getStringVariable(variable, VariableForm.fromString(form),
                    ignoreListeners);
            if (v != null) {
                switch (variable) {
                    case "page":
                        String delimiter = ctx.getTerm("page-range-delimiter");
                        ctx.emit(v.replace("-", delimiter),
                                Token.Type.TEXT, formattingAttributes);
                        break;

                    case "locator":
                    case "number": {
                        List<NumberElement> elements = NumberParser.parse(v);
                        for (int i = 0; i < elements.size(); ++i) {
                            NumberElement e = elements.get(i);
                            SLabel lastLabel = ctx.getLastLabelRendered();
                            if (i > 0 && lastLabel != null && e.getLabel() != null) {
                                lastLabel.render(ctx, i);
                            }
                            ctx.emit(e.getText(), Token.Type.TEXT,
                                    formattingAttributes);
                        }
                        break;
                    }

                    case "DOI":
                        ctx.emit(v, Token.Type.DOI, formattingAttributes);
                        break;

                    case "URL":
                        ctx.emit(v, Token.Type.URL, formattingAttributes);
                        break;

                    default:
                        ctx.emit(v, Token.Type.TEXT, formattingAttributes);
                        break;
                }
            }
        } else if (macro != null && !macro.isEmpty()) {
            SMacro sm = ctx.getMacro(macro);
            if (formattingAttributes == 0) {
                sm.render(ctx);
            } else {
                RenderContext tmp = new RenderContext(ctx);
                sm.render(tmp);
                ctx.emit(tmp.getResult(), formattingAttributes);
            }
        } else if (term != null && !term.isEmpty()) {
            ctx.emit(ctx.getTerm(term, LTerm.Form.fromString(form)), formattingAttributes);
        } else if (value != null) {
            ctx.emit(value, formattingAttributes);
        }
    }
}
