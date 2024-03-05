package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.locale.LDate;
import de.undercouch.citeproc.csl.internal.token.TextToken;
import de.undercouch.citeproc.csl.internal.token.Token;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.time.AnyDateParser;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.PREFIX;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.SUFFIX;

/**
 * A date element from a style file
 * @author Michel Kraeemr
 */
public class SDate implements SRenderingElement {
    private final static String[] NAMES = new String[] { "year", "month", "day" };
    private final String variable;
    private final String form;
    private final String datePartsAttr;
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

        String form = NodeHelper.getAttrValue(node, "form");
        if (!"text".equals(form) && !"numeric".equals(form)) {
            form = null;
        }
        this.form = form;

        if (this.form != null) {
            String datePartsAttr = NodeHelper.getAttrValue(node, "date-parts");
            if ("year-month-day".equals(datePartsAttr) ||
                    "year-month".equals(datePartsAttr) ||
                    "year".equals(datePartsAttr)) {
                this.datePartsAttr = datePartsAttr;
            } else {
                this.datePartsAttr = "year-month-day";
            }
        } else {
            this.datePartsAttr = null;
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
        // fetch date variable but don't notify listeners until we know
        // if we need to render anything or not
        CSLDate date = ctx.getDateVariable(variable, true);

        if (date == null) {
            // notify listeners that the variable was empty
            ctx.getVariableListeners().forEach(v -> v.onFetchDateVariable(variable, null));
            return;
        }

        int[][] dps = date.getDateParts();
        String literal = date.getLiteral();
        if (dps == null && date.getRaw() != null) {
            try {
                // try to parse raw date
                TemporalAccessor ta = AnyDateParser.parse(date.getRaw(),
                        ctx.getLocale().getLang());
                if (ta.isSupported(ChronoField.YEAR)) {
                    if (ta.isSupported(ChronoField.MONTH_OF_YEAR)) {
                        if (ta.isSupported(ChronoField.DAY_OF_MONTH)) {
                            dps = new int[][] {{
                                    ta.get(ChronoField.YEAR),
                                    ta.get(ChronoField.MONTH_OF_YEAR),
                                    ta.get(ChronoField.DAY_OF_MONTH)
                            }};
                        } else {
                            dps = new int[][] {{
                                    ta.get(ChronoField.YEAR),
                                    ta.get(ChronoField.MONTH_OF_YEAR)
                            }};
                        }
                    } else {
                        dps = new int[][] {{
                                ta.get(ChronoField.YEAR)
                        }};
                    }
                }
            } catch (IllegalArgumentException e) {
                if (literal == null) {
                    literal = date.getRaw();
                }
            }
        }

        boolean notifyListenersEmpty = true;
        if (dps != null && dps.length > 0) {
            int[] first = dps[0];
            int[] last = dps[dps.length - 1];
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

            List<SDatePart> dateParts;
            if (form != null && ctx.getLocale().getDateFormats() != null) {
                dateParts = new ArrayList<>();
                LDate d = ctx.getLocale().getDateFormats().get(form);
                if (d != null) {
                    for (SDatePart datePart : d.getDateParts()) {
                        if ("year".equals(datePart.getName())) {
                            dateParts.add(datePart);
                        } else if ("month".equals(datePart.getName()) &&
                                ("year-month-day".equals(datePartsAttr) ||
                                        "year-month".equals(datePartsAttr))) {
                            dateParts.add(datePart);
                        } else if ("day".equals(datePart.getName()) &&
                                "year-month-day".equals(datePartsAttr)) {
                            dateParts.add(datePart);
                        }
                    }
                }
            } else {
                dateParts = this.dateParts;
            }

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
            notifyListenersEmpty = result.getResult().isEmpty();
            ctx.emit(result.getResult());
        } else if (literal != null) {
            notifyListenersEmpty = literal.isEmpty();
            ctx.emit(literal);
        }

        if (notifyListenersEmpty) {
            // notify listeners that we did not render anything
            ctx.getVariableListeners().forEach(v -> v.onFetchDateVariable(variable, null));
        } else {
            // notify listeners that we actually rendered something
            ctx.getVariableListeners().forEach(v -> v.onFetchDateVariable(variable, date));
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
            for (int i = leftTokens.size(); i > 0; --i) {
                Token t = leftTokens.get(i - 1);
                if (t instanceof TextToken) {
                    if (((TextToken)t).getType() == SUFFIX) {
                        leftTokens.remove(i - 1);
                    }
                    break;
                }
            }
            for (Token t : leftTokens) {
                result.emit(t);
            }

            // render en-dash
            result.emit("–");

            // append all tokens from the second buffer to the result but trim
            // the first prefix
            List<Token> rightTokens = right.getResult().getTokens();
            for (int i = 0; i < rightTokens.size(); ++i) {
                Token t = rightTokens.get(i);
                if (t instanceof TextToken) {
                    if (((TextToken)t).getType() == PREFIX) {
                        rightTokens.remove(i);
                    }
                    break;
                }
            }
            for (Token t : rightTokens) {
                result.emit(t);
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
