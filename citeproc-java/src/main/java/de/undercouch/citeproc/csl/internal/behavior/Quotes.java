package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.function.Consumer;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.CLOSE_QUOTE;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.OPEN_QUOTE;

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
            RenderContext tmp = new RenderContext(ctx);
            renderFunction.accept(tmp);

            if (!tmp.getResult().isEmpty()) {
                String openQuote = ctx.getTerm("open-quote");
                ctx.emit(openQuote, OPEN_QUOTE);

                ctx.emit(tmp.getResult());

                String closeQuote = ctx.getTerm("close-quote");
                ctx.emit(closeQuote, CLOSE_QUOTE);
            }
        } else {
            renderFunction.accept(ctx);
        }
    }
}
