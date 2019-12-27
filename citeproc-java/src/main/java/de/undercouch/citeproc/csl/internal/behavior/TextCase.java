package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.helper.NodeHelper;
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
                .map(this::transform)
                .forEach(ctx::emit);
    }

    /**
     * Transforms the text of a token
     * @param t the token
     * @return the new token with the transformed text
     */
    private Token transform(Token t) {
        String s = t.getText();
        if ("lowercase".equals(textCase)) {
            s = s.toLowerCase();
        } else if ("uppercase".equals(textCase)) {
            s = s.toUpperCase();
        } else if ("capitalize-first".equals(textCase)) {
            s = StringUtils.capitalize(s);
        } else if ("capitalize-all".equals(textCase)) {
            s = WordUtils.capitalize(s);
        }
        return new Token.Builder(t)
                .text(s)
                .build();
    }
}
