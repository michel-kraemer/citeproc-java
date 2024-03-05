package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.token.TextToken;
import de.undercouch.citeproc.csl.internal.token.Token;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.w3c.dom.Node;

import java.util.function.Consumer;

/**
 * Wraps around a render function and transforms its result in terms of text
 * case (uppercase, lowercas, capitalize, etc.).
 * @author Michel Kraemer
 */
public class TextCase implements Behavior {
    private final String textCase;

    /**
     * Parses an XML node and determines what kind of transformation to apply
     * @param node the XML node
     */
    public TextCase(Node node) {
        textCase = NodeHelper.getAttrValue(node, "text-case");
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        RenderContext child = new RenderContext(ctx);
        renderFunction.accept(child);
        child.getResult().getTokens().stream()
                .map(t -> transform(t, ctx))
                .forEach(ctx::emit);
    }

    /**
     * Transforms the text of a token
     * @param t the token
     * @param ctx the current render context
     * @return the new token with the transformed text
     */
    private Token transform(Token t, RenderContext ctx) {
        if (t instanceof TextToken) {
            TextToken tt = (TextToken)t;
            return tt.copyWithText(applyTo(tt.getText(), ctx));
        }
        return t;
    }

    /**
     * Apply the text-case rules to the given string
     * @param s the string to transform
     * @param ctx the current render context
     * @return the transformed string
     */
    public String applyTo(String s, RenderContext ctx) {
        if ("lowercase".equals(textCase)) {
            s = s.toLowerCase();
        } else if ("uppercase".equals(textCase)) {
            s = s.toUpperCase();
        } else if ("capitalize-first".equals(textCase)) {
            s = StringUtils.capitalize(s);
        } else if ("capitalize-all".equals(textCase)) {
            s = WordUtils.capitalize(s);
        } else if ("title".equals(textCase)) {
            // only apply title case if the current language is English
            if (ctx.getItemLocale().getLanguage().equals("en")) {
                s = StringHelper.toTitleCase(s);
            }
        }
        return s;
    }
}
