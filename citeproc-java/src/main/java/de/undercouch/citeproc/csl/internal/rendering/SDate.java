package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * A date element from a style file
 * @author Michel Kraeemr
 */
public class SDate implements SRenderingElement {
    private final static String[] NAMES = new String[] { "year", "month", "day" };
    private final String variable;
    private final List<SDatePart> dateParts = new ArrayList<>();
    private final Affixes affixes;

    /**
     * Creates the date element from an XML node
     * @param node the XML node
     */
    public SDate(Node node) {
        variable = NodeHelper.getAttrValue(node, "variable");
        if (variable == null || variable.isEmpty()) {
            throw new IllegalStateException("Date element does not select a variable");
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node c = children.item(i);
            String nodeName = c.getNodeName();
            if ("date-part".equals(nodeName)) {
                dateParts.add(new SDatePart(c));
            }
        }

        affixes = new Affixes(node);
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(this::renderInternal).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        CSLDate date = ctx.getDateVariable(variable);
        if (date == null) {
            return;
        }

        if (date.getDateParts().length > 0) {
            int[] first = date.getDateParts()[0];
            int[] last = date.getDateParts()[date.getDateParts().length - 1];
            if (first.length != last.length) {
                throw new IllegalStateException("Elements in date range must " +
                        "have the same length");
            }

            // Perform an algorithm that merges dates in date ranges. For
            // example, the dates [2019-12-14, 2019-12-24] will be merged to
            // "14-24 December 2019", [2019-01-01, 2019-12-31] will be merged
            // to "01 January-31 December 2019", and [2018-01-01, 2019-12-31]
            // will be merged to "01 January 2018-31 December 2019". This
            // algorithm also works if the date to render is not a range.
            RenderContext left = new RenderContext(ctx);
            RenderContext right = new RenderContext(ctx);
            RenderContext result = new RenderContext(ctx);

            for (SDatePart dp : dateParts) {
                // determine which part to render
                int len = ArrayUtils.indexOf(NAMES, dp.getName());

                // merge if there are more, less significant parts
                boolean shouldMerge = first.length > len;

                // but do not merge if the more significant parts differ
                // from each other, because in this case, we would have to
                // merge at their position
                if (shouldMerge) {
                    for (int i = 0; i <= len; ++i) {
                        if (first[i] != last[i]) {
                            shouldMerge = false;
                            break;
                        }
                    }
                }

                if (shouldMerge) {
                    // merge by appending left and right to the result
                    merge(left, right, result);

                    // reset left and right
                    left = new RenderContext(ctx);
                    right = new RenderContext(ctx);

                    // render the current part
                    dp.setDate(first);
                    dp.render(result);
                } else {
                    // push first and last date to buffers until we merge them
                    dp.setDate(first);
                    dp.render(left);
                    dp.setDate(last);
                    dp.render(right);
                }
            }

            // merge anything that is left
            merge(left, right, result);

            // emit the final result
            ctx.emit(result.getResult());
        }
    }

    /**
     * Merge two token buffers by appending them to a result buffer with
     * an en-dash as separator
     * @param left a render context holding the first token buffer
     * @param right a render context holding the second token buffer
     * @param result a render context holding the result buffer
     */
    private void merge(RenderContext left, RenderContext right, RenderContext result) {
        if (!left.getResult().isEmpty() && !right.getResult().isEmpty()) {
            // append all tokens from the first buffer to the result but trim
            // the last suffix
            List<Token> leftTokens = left.getResult().getTokens();
            for (int i = 0; i < leftTokens.size(); ++i) {
                Token t = leftTokens.get(i);
                if (i < leftTokens.size() - 1 || t.getType() != Token.Type.SUFFIX) {
                    result.emit(t);
                }
            }

            // render en-dash
            result.emit("\u2013");

            // append all tokens from the second buffer to the result but trim
            // the first prefix
            List<Token> rightTokens = right.getResult().getTokens();
            for (int i = 0; i < rightTokens.size(); ++i) {
                Token t = rightTokens.get(i);
                if (i > 0 || t.getType() != Token.Type.PREFIX) {
                    result.emit(t);
                }
            }
        } else if (!left.getResult().isEmpty()) {
            // second buffer is empty. only render the first one.
            result.emit(left.getResult());
        } else if (!right.getResult().isEmpty()) {
            // first buffer is empty. only render the second one.
            result.emit(right.getResult());
        }
    }
}
