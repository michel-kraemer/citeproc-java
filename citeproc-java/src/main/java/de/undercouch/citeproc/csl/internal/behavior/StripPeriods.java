package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.token.TextToken;
import de.undercouch.citeproc.csl.internal.token.Token;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.function.Consumer;

/**
 * Wraps around a render function and removes all periods from its result
 * @author Michel Kraemer
 */
public class StripPeriods implements Behavior {
    private final boolean stripPeriods;

    /**
     * Default constructor that does not strip periods
     */
    public StripPeriods() {
        stripPeriods = false;
    }

    /**
     * Parses an XML node and determines if periods should be removed or not
     * @param node the XML node
     */
    public StripPeriods(Node node) {
        stripPeriods = NodeHelper.getAttrValueBool(node, "strip-periods", false);
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        if (stripPeriods) {
            RenderContext child = new RenderContext(ctx);
            renderFunction.accept(child);
            child.getResult().getTokens().stream()
                    .map(this::transform)
                    .forEach(ctx::emit);
        } else {
            renderFunction.accept(ctx);
        }
    }

    /**
     * Remove all periods from a token's text
     * @param t the token
     * @return the new token with periods removed
     */
    private Token transform(Token t) {
        if (t instanceof TextToken) {
            TextToken tt = (TextToken)t;
            return tt.copyWithText(StringUtils.remove(tt.getText(), '.'));
        }
        return t;
    }
}
