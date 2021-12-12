package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.SmartQuotes;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.NORMAL;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.UNDEFINED;

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

    protected boolean convertLinks = false;

    /**
     * Performs post-processing on the given buffer. Alters the buffer's
     * contents. Be sure to make a copy of the buffer before calling this method.
     * @param buffer the buffer to process
     * @param ctx the render context in which the buffer was created
     */
    protected void postProcess(TokenBuffer buffer, RenderContext ctx) {
        List<Token> tokens = buffer.getTokens();

        // convert straight quotation marks to curly ones
        SmartQuotes sq = new SmartQuotes(ctx.getTerm("open-inner-quote"),
                ctx.getTerm("close-inner-quote"), ctx. getTerm("open-quote"),
                ctx.getTerm("close-quote"), ctx.getLocale().getLang());
        for (int i = 0; i < tokens.size(); ++i) {
            Token t = tokens.get(i);
            if (t.getType() == Token.Type.TEXT ||
                    t.getType() == Token.Type.PREFIX ||
                    t.getType() == Token.Type.SUFFIX ||
                    t.getType() == Token.Type.DELIMITER) {
                tokens.set(i, new Token.Builder(t)
                        .text(sq.apply(t.getText()))
                        .build());
            }
        }

        // swap punctuation and closing quotation marks if necessary
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
            boolean mergeableSuffix = t1.getType() == Token.Type.SUFFIX &&
                    !t1.getText().startsWith(")");
            if (t1.getType() == Token.Type.PREFIX || mergeableSuffix ||
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

        // filter DOI prefix
        if (convertLinks) {
            for (int i = 1; i < tokens.size(); ++i) {
                Token t0 = tokens.get(i - 1);
                Token t1 = tokens.get(i);
                if (t1.getType() == Token.Type.DOI &&
                        t0.getType() == Token.Type.PREFIX &&
                        t0.getText().matches("^https?://doi.org/?$")) {
                    // add doi.org if necessary
                    String url = addDOIPrefix(t1.getText());
                    if (!url.equals(t1.getText())) {
                        tokens.set(i, new Token.Builder(t1)
                                .text(url)
                                .build());
                    }

                    // Remove unnecessary prefix
                    tokens.remove(i - 1);

                    // no need to decrease i because even if the next t1 would
                    // be a DOI, t0 could not be a PREFIX
                    // --i;
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
    public void setConvertLinks(boolean convert) {
        convertLinks = convert;
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
     * Convert a URL to a link
     * @param str the URL
     * @return the link
     */
    protected String formatURL(String str) {
        String escapedStr = escape(str);
        return doFormatLink(escapedStr, escapedStr);
    }

    /**
     * Prepends 'https://doi.org/' to the given string if it is not an absolute
     * URL yet.
     * @param str the string
     * @return the string with the prefix
     */
    protected String addDOIPrefix(String str) {
        if (!str.matches("^https?://.*$")) {
            str = "https://doi.org/" + str;
        }
        return str;
    }

    /**
     * Convert a DOI to a link
     * @param str the DOI
     * @return the link
     */
    protected String formatDOI(String str) {
        String uri = addDOIPrefix(str);
        return doFormatLink(escape(str), escape(uri));
    }

    /**
     * Convert the given string to a link
     * @param text the string to convert
     * @param uri the URI the link should point to
     * @return the link
     */
    protected abstract String doFormatLink(String text, String uri);

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

        int cfs = UNDEFINED;
        int cfv = UNDEFINED;
        int cfw = UNDEFINED;
        int ctd = UNDEFINED;
        int cva = UNDEFINED;

        for (Token t : buffer.getTokens()) {
            int tokenFormattingAttributes = t.getFormattingAttributes();
            int tfs = FormattingAttributes.getFontStyle(tokenFormattingAttributes);
            int tfv = FormattingAttributes.getFontVariant(tokenFormattingAttributes);
            int tfw = FormattingAttributes.getFontWeight(tokenFormattingAttributes);
            int ttd = FormattingAttributes.getTextDecoration(tokenFormattingAttributes);
            int tva = FormattingAttributes.getVerticalAlign(tokenFormattingAttributes);

            compareFormattingAttribute(cfs, tfs, this::closeFontStyle, result);
            compareFormattingAttribute(cfv, tfv, this::closeFontVariant, result);
            compareFormattingAttribute(cfw, tfw, this::closeFontWeight, result);
            compareFormattingAttribute(ctd, ttd, this::closeTextDecoration, result);
            compareFormattingAttribute(cva, tva, this::closeVerticalAlign, result);

            compareFormattingAttribute(tva, cva, this::openVerticalAlign, result);
            compareFormattingAttribute(ttd, ctd, this::openTextDecoration, result);
            compareFormattingAttribute(tfw, cfw, this::openFontWeight, result);
            compareFormattingAttribute(tfv, cfv, this::openFontVariant, result);
            compareFormattingAttribute(tfs, cfs, this::openFontStyle, result);

            cfs = tfs;
            cfv = tfv;
            cfw = tfw;
            ctd = ttd;
            cva = tva;

            if (convertLinks && (t.getType() == Token.Type.URL ||
                    t.getType() == Token.Type.DOI)) {
                // convert URLs and DOIs to links
                String link;
                if (t.getType() == Token.Type.URL) {
                    link = formatURL(t.getText());
                } else {
                    link = formatDOI(t.getText());
                }
                result.append(link);
            } else {
                // render escaped token
                result.append(escape(t.getText()));
            }
        }

        // close remaining formatting attributes
        compareFormattingAttribute(cfs, 0, this::closeFontStyle, result);
        compareFormattingAttribute(cfv, 0, this::closeFontVariant, result);
        compareFormattingAttribute(cfw, 0, this::closeFontWeight, result);
        compareFormattingAttribute(ctd, 0, this::closeTextDecoration, result);
        compareFormattingAttribute(cva, 0, this::closeVerticalAlign, result);

        return result.toString();
    }

    /**
     * Compare two formatting attributes with each other and, if necessary,
     * call the specified function with the second attribute and append the
     * result to the given {@link StringBuilder}.
     * @param a the first formatting attribute
     * @param b the second formatting attribute
     * @param f the function to call
     * @param result the {@link StringBuilder} to append the result to
     */
    protected void compareFormattingAttribute(int a, int b,
            Function<Integer, String> f, StringBuilder result) {
        if (a != b && a != UNDEFINED && !(a == NORMAL && b == UNDEFINED)) {
            String str = f.apply(a);
            if (str != null) {
                result.append(str);
            }
        }
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
