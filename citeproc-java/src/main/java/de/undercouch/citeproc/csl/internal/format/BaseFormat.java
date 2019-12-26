package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A base class for output formats
 * @author Michel Kraemer
 */
abstract public class BaseFormat implements Format {
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

    /**
     * Performs post-processing on the given buffer. Alters the buffer's
     * contents. Be sure to make a copy of the buffer before calling this method.
     * @param buffer the buffer to process
     * @param ctx the render context in which the buffer was created
     */
    protected void postProcess(TokenBuffer buffer, RenderContext ctx) {
        // swap punctuation and closing quotation marks if necessary
        List<Token> tokens = buffer.getTokens();
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

            Token t1 = tokens.get(i);
            if (t1.getType() == Token.Type.PREFIX ||
                    t1.getType() == Token.Type.SUFFIX ||
                    t1.getType() == Token.Type.DELIMITER) {
                // collect as much preceding text as necessary
                Token t0 = tokens.get(j);
                String t0str = t0.getText();
                if (t0str.length() < t1.getText().length()) {
                    StringBuilder pre = new StringBuilder(t0str);
                    while (pre.length() < t1.getText().length() && j > 0) {
                        --j;
                        t0 = tokens.get(j);
                        pre.insert(0, t0.getText());
                    }
                    t0str = pre.toString();
                }

                // remove overlap from t1
                int overlap = StringHelper.overlap(t0str, t1.getText());
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

    @Override
    public String formatCitation(RenderContext ctx) {
        TokenBuffer buffer = new TokenBuffer();
        buffer.append(ctx.getResult());
        postProcess(buffer, ctx);
        return doFormatCitation(buffer, ctx);
    }

    /**
     * Convert a citation from a post-processed buffer to a string
     * @param buffer the post-processed buffer to format
     * @param ctx the render context holding the original, non-post-processed
     * buffer and parameters
     * @return the formatted citation
     */
    protected abstract String doFormatCitation(TokenBuffer buffer, RenderContext ctx);

    @Override
    public String formatBibliographyEntry(RenderContext ctx) {
        TokenBuffer buffer = new TokenBuffer();
        buffer.append(ctx.getResult());
        postProcess(buffer, ctx);
        return doFormatBibliographyEntry(buffer, ctx);
    }

    /**
     * Convert a bibliography entry from a post-processed buffer to a string
     * @param buffer the post-processed buffer to format
     * @param ctx the render context holding the original, non-post-processed
     * buffer and parameters
     * @return the formatted bibliography entry
     */
    protected abstract String doFormatBibliographyEntry(TokenBuffer buffer, RenderContext ctx);
}
