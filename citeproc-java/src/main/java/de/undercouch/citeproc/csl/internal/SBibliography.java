package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private final SLayout layout;

    /**
     * Construct the bibliography element from an XML node
     * @param node the XML node
     */
    public SBibliography(Node node) {
        Node layoutNode = NodeHelper.findDirectChild(node, "layout");
        if (layoutNode == null) {
            layout = null;
        } else {
            layout = new SLayout(layoutNode);
        }
    }

    @Override
    public void render(RenderContext ctx) {
        if (layout == null) {
            return;
        }

        RenderContext tmp = new RenderContext(ctx);
        layout.render(tmp);

        // swap punctuation and closing quotation marks if necessary
        List<Token> tokens = tmp.getResult().getTokens();
        if (ctx.getLocale().getStyleOptions().isPunctuationInQuote()) {
            for (int i = 0; i < tokens.size() - 1; ++i) {
                Token t0 = tokens.get(i);
                Token t1 = tokens.get(i + 1);
                if (t0.getType() == Token.Type.CLOSE_QUOTE &&
                        StringUtils.startsWithAny(t1.getText(), ",", ".")) {
                    String nextText = t1.getText();
                    String punctuation = nextText.substring(0, 1);
                    String rest = nextText.substring(1);
                    tokens.add(i, new Token(punctuation, t1.getType()));
                    ++i;
                    tokens.set(i + 1, new Token(rest, t1.getType()));
                }
            }
        }

        // remove extraneous prefixes, suffixes, and delimiters
        for (int i = 1; i < tokens.size(); ++i) {
            Token t0 = tokens.get(i - 1);
            Token t1 = tokens.get(i);
            if (t1.getType() == Token.Type.PREFIX ||
                    t1.getType() == Token.Type.SUFFIX ||
                    t1.getType() == Token.Type.DELIMITER) {
                int overlap = StringHelper.overlap(t0.getText(), t1.getText());
                if (overlap > 0) {
                    String rest = t1.getText().substring(overlap);
                    if (rest.isEmpty()) {
                        tokens.remove(i);
                        i--;
                    } else {
                        tokens.set(i, new Token(rest, t1.getType()));
                    }
                }
            }
        }

        ctx.emit(tmp.getResult());
    }
}
