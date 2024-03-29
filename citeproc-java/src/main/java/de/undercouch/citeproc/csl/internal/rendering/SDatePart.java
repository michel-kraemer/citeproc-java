package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.StripPeriods;
import de.undercouch.citeproc.csl.internal.locale.LTerm;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.Objects;

/**
 * A date-part element from a style file
 * @author Michel Kraemer
 */
public class SDatePart implements SElement {
    private int[] date;

    private final String name;
    private final String form;
    private final Affixes affixes;
    private final StripPeriods stripPeriods;
    private final String rangeDelimiter;

    /**
     * Creates the date-part element from an XML node
     * @param node the XML node
     */
    public SDatePart(Node node) {
        name = NodeHelper.getAttrValue(node, "name");
        if (name == null) {
            throw new IllegalStateException("Missing date part name");
        }
        if (!name.equals("year") && !name.equals("month") && !name.equals("day")) {
            throw new IllegalStateException("Unknown date part name: " + name);
        }

        form = NodeHelper.getAttrValue(node, "form");
        affixes = new Affixes(node);
        stripPeriods = new StripPeriods(node);

        String rd = NodeHelper.getAttrValue(node, "range-delimiter");
        rangeDelimiter = Objects.requireNonNullElse(rd, "–");
    }

    @Override
    public void render(RenderContext ctx) {
        affixes.wrap(stripPeriods.wrap(this::renderInternal)).accept(ctx);
    }

    private void renderInternal(RenderContext ctx) {
        String value = null;
        switch (name) {
            case "year":
                if (date.length > 0) {
                    value = renderYear(date[0], ctx);
                }
                break;

            case "month":
                if (date.length > 1) {
                    value = renderMonth(date[1], ctx);
                }
                break;

            case "day":
                if (date.length > 2) {
                    value = renderDay(date[2]);
                }
                break;
        }

        if (value != null) {
            ctx.emit(value);
        }
    }

    private String renderYear(int year, RenderContext ctx) {
        if (year < 0) {
            return (-year) + ctx.getTerm("bc");
        } else if (year < 1000) {
            return year + ctx.getTerm("ad");
        }
        return String.valueOf(year);
    }

    private String renderMonth(int month, RenderContext ctx) {
        String value;
        String p = StringUtils.leftPad(String.valueOf(month), 2, '0');
        if ("short".equals(form)) {
            value = ctx.getTerm("month-" + p, LTerm.Form.SHORT);
        } else if ("numeric".equals(form)) {
            value = String.valueOf(month);
        } else if ("numeric-leading-zeros".equals(form)) {
            value = p;
        } else {
            value = ctx.getTerm("month-" + p);
        }
        return value;
    }

    private String renderDay(int day) {
        String value = String.valueOf(day);
        if ("numeric-leading-zeros".equals(form)) {
            value = StringUtils.leftPad(value, 2, '0');
        }
        return value;
    }

    /**
     * Get the name of the date part to render (i.e. {@code year}, {@code month},
     * or {@code day})
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the date to render
     * @param date the date
     */
    public void setDate(int[] date) {
        this.date = date;
    }

    /**
     * Get the delimiter to use to express ranges between this date part and
     * another one with the same name (i.e. between two years, two months,
     * or two days). The default delimiter is an en-dash.
     * @return the delimiter (never {@code null})
     */
    public String getRangeDelimiter() {
        return rangeDelimiter;
    }
}
