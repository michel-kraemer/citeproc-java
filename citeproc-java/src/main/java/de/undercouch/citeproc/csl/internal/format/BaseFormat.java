package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.token.TextToken;
import de.undercouch.citeproc.csl.internal.token.Token;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.Display;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.SmartQuotes;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
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
     * A list of all formatting attributes managed by {@link #format(TokenBuffer)}
     */
    private enum Format {
        FontStyle,
        FontVariant,
        FontWeight,
        TextDecoration,
        VerticalAlign;
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
            if (t instanceof TextToken) {
                TextToken tt = (TextToken)t;
                TextToken.Type type = tt.getType();
                if (type == TextToken.Type.TEXT ||
                        type == TextToken.Type.PREFIX ||
                        type == TextToken.Type.SUFFIX ||
                        type == TextToken.Type.DELIMITER) {
                    tokens.set(i, tt.copyWithText(sq.apply(tt.getText())));
                }
            }
        }

        // swap punctuation and closing quotation marks if necessary
        if (ctx.getLocale().getStyleOptions().isPunctuationInQuote()) {
            for (int i = 0; i < tokens.size() - 1; ++i) {
                Token ti = tokens.get(i);
                if (!(ti instanceof TextToken)) {
                    continue;
                }
                TextToken t0 = (TextToken)ti;

                int j = findNextText(tokens, i);
                if (j < 0) {
                    break;
                }
                TextToken t1 = (TextToken)tokens.get(j);

                if (t0.getType() == TextToken.Type.CLOSE_QUOTE &&
                        StringUtils.startsWithAny(t1.getText(), ",", ".")) {
                    String nextText = t1.getText();
                    String punctuation = nextText.substring(0, 1);
                    String rest = nextText.substring(1);
                    tokens.add(i, t1.copyWithText(punctuation));
                    tokens.set(j + 1, t1.copyWithText(rest));
                    i = j;
                }
            }
        }

        // remove extraneous prefixes, suffixes, and delimiters
        for (int i = 1; i < tokens.size(); ++i) {
            Token ti = tokens.get(i);
            if (!(ti instanceof TextToken)) {
                continue;
            }
            TextToken t1 = (TextToken)ti;

            boolean mergeableSuffix = t1.getType() == TextToken.Type.SUFFIX &&
                    !t1.getText().startsWith(")");
            if (t1.getType() == TextToken.Type.PREFIX || mergeableSuffix ||
                    t1.getType() == TextToken.Type.DELIMITER) {
                int j = findPreviousNonQuote(tokens, i);
                if (j < 0) {
                    continue;
                }

                // collect as much preceding text as necessary
                TextToken t0 = (TextToken)tokens.get(j);
                String t0str = t0.getText();
                if (t0str.length() < t1.getText().length()) {
                    StringBuilder pre = new StringBuilder(t0str);
                    while (pre.length() < t1.getText().length() && j > 0) {
                        --j;
                        Token tj = tokens.get(j);
                        if (!(tj instanceof TextToken)) {
                            continue;
                        }
                        t0 = (TextToken)tj;
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
                        tokens.set(i, t1.copyWithText(rest));
                    }
                }
            }
        }

        // merge punctuation
        for (int i = 1; i < tokens.size(); ++i) {
            Token ti = tokens.get(i);
            if (!(ti instanceof TextToken)) {
                continue;
            }
            TextToken t1 = (TextToken)ti;

            int j = findPreviousNonQuote(tokens, i);
            if (j < 0) {
                continue;
            }
            TextToken t0 = (TextToken)tokens.get(j);

            if (!t0.getText().isEmpty() && !t1.getText().isEmpty() &&
                    (t1.getType() == TextToken.Type.PREFIX ||
                            t1.getType() == TextToken.Type.SUFFIX ||
                            t1.getType() == TextToken.Type.DELIMITER)) {
                // check if we need to merge the last character of t0 with
                // the first one of t1
                String lookup = t0.getText().substring(t0.getText().length() - 1) +
                        t1.getText().charAt(0);
                String replacement = MERGE_PUNCTUATION_MAP.get(lookup);

                if (replacement != null) {
                    // replace last character in t0
                    String nt0 = t0.getText().substring(0, t0.getText().length() - 1) +
                            replacement;
                    tokens.set(j, t0.copyWithText(nt0));

                    // remove first character from t1 and remove t1 if it's empty
                    String rest = t1.getText().substring(1);
                    if (rest.isEmpty()) {
                        tokens.remove(i);
                        i--;
                    } else {
                        tokens.set(i, t1.copyWithText(rest));
                    }
                }
            }
        }

        // filter DOI prefix
        if (convertLinks) {
            for (int i = 1; i < tokens.size(); ++i) {
                Token ti = tokens.get(i);
                if (!(ti instanceof TextToken)) {
                    continue;
                }
                TextToken t1 = (TextToken)ti;

                int j = findPreviousText(tokens, i);
                if (j < 0) {
                    continue;
                }
                TextToken t0 = (TextToken)tokens.get(j);

                if (t1.getType() == TextToken.Type.DOI &&
                        t0.getType() == TextToken.Type.PREFIX &&
                        t0.getText().matches("^https?://doi.org/?$")) {
                    // add doi.org if necessary
                    String url = addDOIPrefix(t1.getText());
                    if (!url.equals(t1.getText())) {
                        tokens.set(i, t1.copyWithText(url));
                    }

                    // Remove unnecessary prefix
                    tokens.remove(j);
                }
            }
        }
    }

    /**
     * Search the list of tokens from {@code i + 1} up to {@code tokens.size()}
     * and find the first text token
     * @param tokens the list of tokens
     * @param i the index of the token after which the search should start
     * @return the index of the first text token after {@code i}, or {@code -1}
     * if there is no such token
     */
    private static int findNextText(List<Token> tokens, int i) {
        for (int j = i + 1; j < tokens.size(); ++j) {
            if (tokens.get(j) instanceof TextToken) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Search the list of tokens from {@code i - 1} down to {@code 0} and find
     * the first text token
     * @param tokens the list of tokens
     * @param i the index of the token before which the search should start
     * @return the index of the first text token before {@code i}, or {@code -1}
     * if there is no such token
     */
    private static int findPreviousText(List<Token> tokens, int i) {
        for (int j = i - 1; j >= 0; --j) {
            if (tokens.get(j) instanceof TextToken) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Search the list of tokens from {@code i - 1} down to {@code 0} and find
     * the first text token that is not a closing quotation mark
     * @param tokens the list of tokens
     * @param i the index of the token before which the search should start
     * @return the index of the first text token before {@code i} that is not
     * a closing quotation mark, or {@code -1} if there is no such token
     */
    private static int findPreviousNonQuote(List<Token> tokens, int i) {
        for (int j = i - 1; j >= 0; --j) {
            Token t = tokens.get(j);
            if (t instanceof TextToken &&
                    ((TextToken)t).getType() != TextToken.Type.CLOSE_QUOTE) {
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
    public String formatBibliographyEntry(RenderContext ctx, int index) {
        TokenBuffer buffer = new TokenBuffer();
        buffer.append(ctx.getResult());
        postProcess(buffer, ctx);
        return doFormatBibliographyEntry(buffer, ctx, index);
    }

    /**
     * Convert a bibliography entry from a post-processed buffer to a string
     * @param buffer the post-processed buffer to format
     * @param ctx the render context holding the original, non-post-processed
     * buffer and parameters
     * @param index the index of the entry
     * @return the formatted bibliography entry
     */
    protected abstract String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index);

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
     * Prepends {@code https://doi.org/} to the given string if it is not an
     * absolute URL yet.
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

        List<Pair<Format, Integer>> formattingStack = new ArrayList<>();

        for (Token t : buffer.getTokens()) {
            EnumMap<Format, Integer> tokenFormattingAttributes =
                    new EnumMap<>(Format.class);
            tokenFormattingAttributes.put(Format.FontStyle,
                    FormattingAttributes.getFontStyle(t.getFormattingAttributes()));
            tokenFormattingAttributes.put(Format.FontVariant,
                    FormattingAttributes.getFontVariant(t.getFormattingAttributes()));
            tokenFormattingAttributes.put(Format.FontWeight,
                    FormattingAttributes.getFontWeight(t.getFormattingAttributes()));
            tokenFormattingAttributes.put(Format.TextDecoration,
                    FormattingAttributes.getTextDecoration(t.getFormattingAttributes()));
            tokenFormattingAttributes.put(Format.VerticalAlign,
                    FormattingAttributes.getVerticalAlign(t.getFormattingAttributes()));

            // find formatting attributes that are not in effect any more
            boolean[] closed = new boolean[formattingStack.size()];
            int firstClosed = formattingStack.size();
            for (int i = 0; i < formattingStack.size(); ++i) {
                Pair<Format, Integer> f = formattingStack.get(i);
                int newValue = tokenFormattingAttributes.getOrDefault(
                        f.getKey(), FormattingAttributes.UNDEFINED);
                closed[i] = newValue != f.getValue();
                if (closed[i] && firstClosed == formattingStack.size()) {
                    firstClosed = i;
                }
            }

            // close all attributes from the top of the stack until (and
            // including) the first closed one
            for (int i = formattingStack.size(); i > firstClosed; --i) {
                Pair<Format, Integer> f = formattingStack.get(i - 1);
                closeFormattingAttribute(f.getKey(), f.getValue(), result);
            }

            // remove all closed attributes from the stack
            EnumSet<Format> remaining = EnumSet.allOf(Format.class);
            int j = 0;
            Iterator<Pair<Format, Integer>> iter = formattingStack.iterator();
            while (iter.hasNext()) {
                Pair<Format, Integer> f = iter.next();
                if (closed[j]) {
                    iter.remove();
                } else {
                    remaining.remove(f.getKey());
                }
                ++j;
            }

            // push new attributes to the stack
            for (Format r : remaining) {
                int v = tokenFormattingAttributes.getOrDefault(
                        r, FormattingAttributes.UNDEFINED);
                if (v != FormattingAttributes.UNDEFINED) {
                    formattingStack.add(Pair.of(r, v));
                }
            }

            // Open all attributes still in effect (from the position of the
            // first closed one on). This includes any attributes we've just
            // pushed onto the stack.
            for (int i = firstClosed; i < formattingStack.size(); ++i) {
                Pair<Format, Integer> f = formattingStack.get(i);
                openFormattingAttribute(f.getKey(), f.getValue(), result);
            }

            if (t instanceof TextToken) {
                TextToken tt = (TextToken)t;
                if (convertLinks && (tt.getType() == TextToken.Type.URL ||
                        tt.getType() == TextToken.Type.DOI)) {
                    // convert URLs and DOIs to links
                    String link;
                    if (tt.getType() == TextToken.Type.URL) {
                        link = formatURL(tt.getText());
                    } else {
                        link = formatDOI(tt.getText());
                    }
                    result.append(link);
                } else {
                    // render escaped token
                    result.append(escape(tt.getText()));
                }
            }
        }

        // close remaining formatting attributes
        for (int i = formattingStack.size(); i > 0; --i) {
            Pair<Format, Integer> f = formattingStack.get(i - 1);
            closeFormattingAttribute(f.getKey(), f.getValue(), result);
        }

        return result.toString();
    }

    /**
     * Open the given formatting attribute by calling the respective
     * {@code openXXX()} method
     * @param f the formatting attribute to open
     * @param value the attribute's value
     * @param result the string builder to append the result to
     */
    private void openFormattingAttribute(Format f, int value,
            StringBuilder result) {
        if (value == FormattingAttributes.UNDEFINED ||
                value == FormattingAttributes.NORMAL) {
            return;
        }
        String str = null;
        switch (f) {
            case FontStyle:
                str = openFontStyle(value);
                break;
            case FontVariant:
                str = openFontVariant(value);
                break;
            case FontWeight:
                str = openFontWeight(value);
                break;
            case TextDecoration:
                str = openTextDecoration(value);
                break;
            case VerticalAlign:
                str = openVerticalAlign(value);
                break;
        }
        if (str != null) {
            result.append(str);
        }
    }

    /**
     * Close the given formatting attribute by calling the respective
     * {@code closeXXX()} method
     * @param f the formatting attribute to close
     * @param value the attribute's old value
     * @param result the string builder to append the result to
     */
    private void closeFormattingAttribute(Format f, int value,
            StringBuilder result) {
        if (value == FormattingAttributes.UNDEFINED ||
                value == FormattingAttributes.NORMAL) {
            return;
        }
        String str = null;
        switch (f) {
            case FontStyle:
                str = closeFontStyle(value);
                break;
            case FontVariant:
                str = closeFontVariant(value);
                break;
            case FontWeight:
                str = closeFontWeight(value);
                break;
            case TextDecoration:
                str = closeTextDecoration(value);
                break;
            case VerticalAlign:
                str = closeVerticalAlign(value);
                break;
        }
        if (str != null) {
            result.append(str);
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

    /**
     * Generate text that enables the given display attribute
     * @param display the display attribute to enable
     * @return the generated text or {@code null}
     */
    protected String openDisplay(Display display) {
        return null;
    }

    /**
     * Generate text that disables the given display attribute
     * @param display the display attribute to disable
     * @return the generated text or {@code null}
     */
    protected String closeDisplay(Display display) {
        return null;
    }
}
