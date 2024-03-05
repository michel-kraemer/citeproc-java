package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.CharSet;
import org.w3c.dom.Node;

import java.util.regex.Pattern;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.DELIMITER;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.PREFIX;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.SUFFIX;

/**
 * A layout element inside a citation element in a style file
 * @author Michel Kraemer
 */
public class SCitationLayout extends SRenderingElementContainerElement {
    private static final CharSet PUNCTUATION_END = CharSet.getInstance(":.,;!?)]&");
    private static final CharSet PUNCTUATION_START = CharSet.getInstance("([&");
    private static final Pattern TRIM_END = Pattern.compile("\\p{Space}+$");
    private static final Pattern IGNORE_END = Pattern.compile("[\\p{Space}\\p{Pf}\"']+$");

    private final Affixes affixes;
    private final int formattingAttributes;
    private final String delimiter;

    /**
     * Construct the layout element from an XML node
     * @param node the XML node
     */
    public SCitationLayout(Node node) {
        super(node);
        affixes = new Affixes(node);
        formattingAttributes = FormattingAttributes.of(node);
        delimiter = NodeHelper.getAttrValue(node, "delimiter");
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(this::renderInternal).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        RenderContext tmp = new RenderContext(ctx);
        for (CSLCitationItem item : ctx.getCitation().getCitationItems()) {
            RenderContext innerTmp = new RenderContext(ctx, item);
            innerTmp.reset();
            super.render(innerTmp);

            if (delimiter != null && !tmp.getResult().isEmpty()) {
                tmp.emit(delimiter, DELIMITER);
            }

            if (item.getPrefix() != null) {
                String prefix = item.getPrefix();
                if (needsAppendSpace(prefix)) {
                    prefix = TRIM_END.matcher(prefix).replaceAll("") + " ";
                }
                tmp.emit(prefix, PREFIX);
            }

            tmp.emit(innerTmp.getResult());

            if (item.getSuffix() != null) {
                String suffix = item.getSuffix();
                if (needsPrependSpace(suffix)) {
                    suffix = " " + suffix;
                }
                tmp.emit(suffix, SUFFIX);
            }
        }
        ctx.emit(tmp.getResult(), formattingAttributes);
    }

    /**
     * Check if we need to append a space character to the given prefix
     * @param prefix the prefix
     * @return {@code true} if we need to append a space character
     */
    private static boolean needsAppendSpace(String prefix) {
        if (prefix.isEmpty()) {
            return false;
        }

        // ignore some characters at the end
        prefix = IGNORE_END.matcher(prefix).replaceAll("");

        // now check last character
        char c = prefix.charAt(prefix.length() - 1);
        return Character.isLetterOrDigit(c) || PUNCTUATION_END.contains(c);
    }

    /**
     * Check if we need to prepend a space character to the given suffix
     * @param suffix the suffix
     * @return {@code true} if we need to prepend a space character
     */
    private static boolean needsPrependSpace(String suffix) {
        if (suffix.isEmpty()) {
            return false;
        }

        char c = suffix.charAt(0);
        return Character.isLetterOrDigit(c) || PUNCTUATION_START.contains(c);
    }
}
