package de.undercouch.citeproc.csl.internal;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private SLayout layout;

    /**
     * Construct the bibliography element from an XML node
     * @param node the XML node
     */
    public SBibliography(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            if ("layout".equals(c.getNodeName())) {
                layout = new SLayout(c);
            }
        }
    }

    @Override
    public void render(RenderContext ctx) {
        if (layout != null) {
            RenderContext tmp = new RenderContext(ctx);
            layout.render(tmp);

            // swap punctuation and closing quotation marks if necessary
            List<Token> tokens = tmp.getResult().getTokens();
            if (ctx.getLocale().getStyleOptions().isPunctuationInQuote()) {
                for (int i = 0; i < tokens.size(); ++i) {
                    Token t = tokens.get(i);
                    if (t.getType() == Token.Type.CLOSE_QUOTE && i < tokens.size() - 1 &&
                            StringUtils.startsWithAny(tokens.get(i + 1).getText(), ",", ".")) {
                        Token nextToken = tokens.get(i + 1);
                        String nextText = nextToken.getText();
                        String puncutation = nextText.substring(0, 1);
                        String rest = nextText.substring(1);
                        tokens.add(i, new Token(puncutation, Token.Type.TEXT));
                        ++i;
                        tokens.set(i + 1, new Token(rest, nextToken.getType()));
                    }
                }
            }

            ctx.emit(tmp.getResult());
        }
    }
}