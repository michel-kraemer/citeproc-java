package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.IntBuffer;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
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
                    tokens.add(i, new Token.Builder(t1)
                            .text(punctuation)
                            .build());
                    ++i;
                    tokens.set(i + 1, new Token.Builder(t1)
                            .text(rest)
                            .build());
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
                        tokens.set(i, new Token.Builder(t1)
                                .text(rest)
                                .build());
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
                    tokens.set(j, new Token.Builder(t0)
                            .text(nt0)
                            .build());

                    // remove first character from t1 and remove t1 if it's empty
                    String rest = t1.getText().substring(1);
                    if (rest.isEmpty()) {
                        tokens.remove(i);
                        i--;
                    } else {
                        tokens.set(i, new Token.Builder(t1)
                                .text(rest)
                                .build());
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

    /**
     * Escape any formatting instructions specific to the output format
     * @param str the string to escape (may be {@code null})
     * @return the escaped string
     */
    protected String escape(String str) {
        return str;
    }

    /**
     * Format a given token buffer
     * @param buffer the buffer to format
     * @return the formatted string
     */
    protected String format(TokenBuffer buffer) {
        StringBuilder result = new StringBuilder();

        // a stack of formatting attributes currently in effect
        Deque<Integer> currentFormattingAttributes = new ArrayDeque<>();

        for (Token t : buffer.getTokens()) {
            // get formatting attributes of current token
            IntBuffer tokenFormattingAttributes = t.getFormattingAttributes();

            // Close and remove current formatting attributes that are not
            // part of 'tokenFormattingAttributes'. Close them in the order
            // they have been opened.
            Iterator<Integer> iterator = currentFormattingAttributes.iterator();
            while (iterator.hasNext()) {
                int cf = iterator.next();
                if (tokenFormattingAttributes == null ||
                        tokenFormattingAttributes.indexOf(cf) < 0) {
                    iterator.remove();
                    String str = closeFormatting(cf);
                    if (str != null) {
                        result.append(str);
                    }
                }
            }

            // Open formatting attributes from 'tokenFormattingAttributes' if
            // they are not already open. Push them to
            // 'currentFormattingAttributes' to keep the correct order.
            if (tokenFormattingAttributes != null) {
                // iterate through 'tokenFormattingAttributes' from back to
                // front because attributes that have been added later have a
                // lower priority (i.e. preceding attributes may overwrite)
                for (int i = tokenFormattingAttributes.length() - 1; i >= 0; --i) {
                    int f = tokenFormattingAttributes.get(i);
                    if (!currentFormattingAttributes.contains(f)) {
                        currentFormattingAttributes.push(f);
                        String str = openFormatting(f);
                        if (str != null) {
                            result.append(str);
                        }
                    }
                }
            }

            // now escape the token's text and append it to the result
            result.append(escape(t.getText()));
        }

        // close all remaining formatting attributes
        for (int cf : currentFormattingAttributes) {
            String str = closeFormatting(cf);
            if (str != null) {
                result.append(str);
            }
        }

        return result.toString();
    }

    /**
     * Generate text that enables the given formatting attributes in the
     * output format
     * @param formattingAttributes the formatting attributes to enable
     * @return the generated text or {@code null}
     */
    protected String openFormatting(int formattingAttributes) {
        if (formattingAttributes == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        int va = FormattingAttributes.getVerticalAlign(formattingAttributes);
        if (va != 0) {
            String str = openVerticalAlign(va);
            if (str != null) {
                result.append(str);
            }
        }

        int td = FormattingAttributes.getTextDecoration(formattingAttributes);
        if (td != 0) {
            String str = openTextDecoration(td);
            if (str != null) {
                result.append(str);
            }
        }

        int fw = FormattingAttributes.getFontWeight(formattingAttributes);
        if (fw != 0) {
            String str = openFontWeight(fw);
            if (str != null) {
                result.append(str);
            }
        }

        int fv = FormattingAttributes.getFontVariant(formattingAttributes);
        if (fv != 0) {
            String str = openFontVariant(fv);
            if (str != null) {
                result.append(str);
            }
        }

        int fs = FormattingAttributes.getFontStyle(formattingAttributes);
        if (fs != 0) {
            String str = openFontStyle(fs);
            if (str != null) {
                result.append(str);
            }
        }

        if (result.length() == 0) {
            return null;
        }

        return result.toString();
    }

    /**
     * Generate text that disables the given formatting attributes in the
     * output format
     * @param formattingAttributes the formatting attributes to disable
     * @return the generated text or {@code null}
     */
    protected String closeFormatting(int formattingAttributes) {
        if (formattingAttributes == 0) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        int fs = FormattingAttributes.getFontStyle(formattingAttributes);
        if (fs != 0) {
            String str = closeFontStyle(fs);
            if (str != null) {
                result.append(str);
            }
        }

        int fv = FormattingAttributes.getFontVariant(formattingAttributes);
        if (fv != 0) {
            String str = closeFontVariant(fv);
            if (str != null) {
                result.append(str);
            }
        }

        int fw = FormattingAttributes.getFontWeight(formattingAttributes);
        if (fw != 0) {
            String str = closeFontWeight(fw);
            if (str != null) {
                result.append(str);
            }
        }

        int td = FormattingAttributes.getTextDecoration(formattingAttributes);
        if (td != 0) {
            String str = closeTextDecoration(td);
            if (str != null) {
                result.append(str);
            }
        }

        int va = FormattingAttributes.getVerticalAlign(formattingAttributes);
        if (va != 0) {
            String str = closeVerticalAlign(va);
            if (str != null) {
                result.append(str);
            }
        }

        if (result.length() == 0) {
            return null;
        }

        return result.toString();
    }

    /**
     * Generate text that enables the given font style in the output format
     * @param fontStyle the font style to enable
     * @return the generated text or {@code null}
     */
    protected String openFontStyle(int fontStyle) {
        return null;
    }

    /**
     * Generate text that disables the given font style in the output format
     * @param fontStyle the font style to disable
     * @return the generated text or {@code null}
     */
    protected String closeFontStyle(int fontStyle) {
        return null;
    }

    /**
     * Generate text that enables the given font variant in the output format
     * @param fontVariant the font variant to enable
     * @return the generated text or {@code null}
     */
    protected String openFontVariant(int fontVariant) {
        return null;
    }

    /**
     * Generate text that disables the given font variant in the output format
     * @param fontVariant the font variant to disable
     * @return the generated text or {@code null}
     */
    protected String closeFontVariant(int fontVariant) {
        return null;
    }

    /**
     * Generate text that enables the given font weight in the output format
     * @param fontWeight the font weight to enable
     * @return the generated text or {@code null}
     */
    protected String openFontWeight(int fontWeight) {
        return null;
    }

    /**
     * Generate text that disables the given font weight in the output format
     * @param fontWeight the font weight to disable
     * @return the generated text or {@code null}
     */
    protected String closeFontWeight(int fontWeight) {
        return null;
    }

    /**
     * Generate text that enables the given text decoration in the output format
     * @param textDecoration the text decoration to enable
     * @return the generated text or {@code null}
     */
    protected String openTextDecoration(int textDecoration) {
        return null;
    }

    /**
     * Generate text that disables the given text decoration in the output format
     * @param textDecoration the text decoration to disable
     * @return the generated text or {@code null}
     */
    protected String closeTextDecoration(int textDecoration) {
        return null;
    }

    /**
     * Generate text that enables the given vertical alignment in the output format
     * @param verticalAlign the vertical alignment to enable
     * @return the generated text or {@code null}
     */
    protected String openVerticalAlign(int verticalAlign) {
        return null;
    }

    /**
     * Generate text that disables the given vertical alignment in the output format
     * @param verticalAlign the vertical alignment to disable
     * @return the generated text or {@code null}
     */
    protected String closeVerticalAlign(int verticalAlign) {
        return null;
    }
}
