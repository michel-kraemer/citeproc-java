package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bibliography element in a style file
 * @author Michel Kraemer
 */
public class SBibliography implements SElement {
    private final static Map<String, String> MERGE_PUNCTUATION_MAP;
    static {
        Map<String, String> mpm = new HashMap<>();

        mpm.put("!.", "!");
        mpm.put("!:", "!");

        mpm.put("?.", "?");
        mpm.put("?:", "?");

        mpm.put(":!", "!");
        mpm.put(":?", "?");
        mpm.put(":.", ":");

        mpm.put(";!", "!");
        mpm.put(";?", "?");
        mpm.put(";:", ";");
        mpm.put(";.", ";");

        MERGE_PUNCTUATION_MAP = Collections.unmodifiableMap(mpm);
    }

    private final SSort sort;
    private final SLayout layout;

    /**
     * Construct the bibliography element from an XML node
     * @param node the XML node
     */
    public SBibliography(Node node) {
        Node sortNode = NodeHelper.findDirectChild(node, "sort");
        if (sortNode == null) {
            sort = null;
        } else {
            sort = new SSort(sortNode);
        }

        Node layoutNode = NodeHelper.findDirectChild(node, "layout");
        if (layoutNode == null) {
            layout = null;
        } else {
            layout = new SLayout(layoutNode);
        }
    }

    /**
     * Get the sort child element (if there is any)
     * @return the sort child element (or {@code null} if the bibliography
     * element does not have a sort child element)
     */
    public SSort getSort() {
        return sort;
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
            int j = findPreviousNonQuote(tokens, i);
            if (j < 0) {
                continue;
            }

            Token t0 = tokens.get(j);
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

        // merge punctuation
        for (int i = 1; i < tokens.size(); ++i) {
            int j = findPreviousNonQuote(tokens, i);
            if (j < 0) {
                continue;
            }

            Token t0 = tokens.get(j);
            Token t1 = tokens.get(i);

            if (!t0.getText().isEmpty() && !t1.getText().isEmpty() &&
                    (t1.getType() == Token.Type.PREFIX ||
                            t1.getType() == Token.Type.SUFFIX ||
                            t1.getType() == Token.Type.DELIMITER)) {
                // check if we need to merge the last character of t0 with
                // the first one of t1
                String lookup = t0.getText().substring(t0.getText().length() - 1) +
                        t1.getText().substring(0, 1);
                String replacement = MERGE_PUNCTUATION_MAP.get(lookup);

                if (replacement != null) {
                    // replace last character in t0
                    String nt0 = t0.getText().substring(0, t0.getText().length() - 1) +
                            replacement;
                    tokens.set(j, new Token(nt0, t0.getType()));

                    // remove first character from t1 and remove t1 if it's empty
                    String rest = t1.getText().substring(1);
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

    /**
     * Search the list of tokens from {@code i - 1} down to {@code 0} and find
     * the first one that is not a closing quotation mark
     * @param tokens the list of tokens
     * @param i the index of the token where the search should start
     * @return the first token that is not a closing quotation mark
     */
    private static int findPreviousNonQuote(List<Token> tokens, int i) {
        for (int j = i - 1; j >= 0; --j) {
            Token t = tokens.get(j);
            if (t.getType() != Token.Type.CLOSE_QUOTE) {
                return j;
            }
        }
        return -1;
    }
}
