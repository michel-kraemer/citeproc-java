package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.function.Consumer;

/**
 * Wraps around a render function and adds quotation marks
 * @author Michel Kraemer
 */
public class Quotes implements Behavior {
    private final boolean quotes;

    /**
     * Parses an XML node and determines if quotation marks should be added
     * @param node the XML node
     */
    public Quotes(Node node) {
        quotes = Boolean.parseBoolean(NodeHelper.getAttrValue(node, "quotes"));
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        if (quotes) {
            String openQuote = ctx.getTerm("open-quote");
            ctx.emit(openQuote, Token.Type.OPEN_QUOTE);
        }

        renderFunction.accept(ctx);

        if (quotes) {
            String closeQuote = ctx.getTerm("close-quote");
            ctx.emit(closeQuote, Token.Type.CLOSE_QUOTE);
        }
    }
}
