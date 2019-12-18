package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

/**
 * A name element from a style file
 * @author Michel Kraemer
 */
public class SName implements SElement {
    private final String variable;
    private final String and;
    private final String delimiter;
    private final String delimiterPrecedesEtAl;
    private final String delimiterPrecedesLast;
    private final String initializeWith;
    private final String nameAsSortOrder;
    private final String sortSeparator;
    private final Integer etAlMin;
    private final Integer etAlUseFirst;

    /**
     * Create the name element from an XML node
     * @param node the XML node (may be {@code null})
     * @param variable the variable that holds the name (may be {@code null})
     */
    public SName(Node node, String variable) {
        this.variable = variable;

        String delimiter;
        String delimiterPrecedesEtAl;
        String delimiterPrecedesLast;
        String sortSeparator;
        String etAlMin;
        String etAlUseFirst;
        if (node != null) {
            and = NodeHelper.getAttrValue(node, "and");
            initializeWith = NodeHelper.getAttrValue(node, "initialize-with");
            nameAsSortOrder = NodeHelper.getAttrValue(node, "name-as-sort-order");
            delimiter = NodeHelper.getAttrValue(node, "delimiter");
            delimiterPrecedesEtAl = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-et-al");
            delimiterPrecedesLast = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-last");
            sortSeparator = NodeHelper.getAttrValue(node, "sort-separator");
            etAlMin = NodeHelper.getAttrValue(node, "et-al-min");
            etAlUseFirst = NodeHelper.getAttrValue(node, "et-al-use-first");
        } else {
            and = null;
            initializeWith = null;
            nameAsSortOrder = null;
            delimiter = null;
            delimiterPrecedesEtAl = null;
            delimiterPrecedesLast = null;
            sortSeparator = null;
            etAlMin = null;
            etAlUseFirst = null;
        }

        if (delimiter == null) {
            delimiter = ", ";
        }
        this.delimiter = delimiter;

        if (delimiterPrecedesEtAl == null) {
            delimiterPrecedesEtAl = "contextual";
        }
        this.delimiterPrecedesEtAl = delimiterPrecedesEtAl;

        if (delimiterPrecedesLast == null) {
            delimiterPrecedesLast = "contextual";
        }
        this.delimiterPrecedesLast = delimiterPrecedesLast;

        if (sortSeparator == null) {
            sortSeparator = ", ";
        }
        this.sortSeparator = sortSeparator;

        if (etAlMin != null) {
            this.etAlMin = Integer.parseInt(etAlMin);
        } else {
            this.etAlMin = null;
        }

        if (etAlUseFirst != null) {
            this.etAlUseFirst = Integer.parseInt(etAlUseFirst);
        } else {
            this.etAlUseFirst = null;
        }
    }

    @Override
    public void render(RenderContext ctx) {
        CSLName[] names = null;
        if (variable != null) {
            names = ctx.getNameVariable(variable);
        }
        if (names == null) {
            return;
        }

        String and;
        if ("text".equals(this.and)) {
            and = " " + ctx.getTerm("and") + " ";
        } else if ("symbol".equals(this.and)) {
            and = " & ";
        } else {
            and = delimiter;
        }

        // get the maximum number of names to render before "et al."
        int max = -1;
        if (etAlMin != null && etAlUseFirst != null && names.length >= etAlMin) {
            max = etAlUseFirst;
            if (max == 0) {
                // do not render any name
                return;
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.length; ++i) {
            boolean nameAsSort = "all".equals(nameAsSortOrder) ||
                    (i == 0 && "first".equals(nameAsSortOrder));
            builder.append(render(names[i], nameAsSort));

            if (i < names.length - 1) {
                if (i == max - 1) {
                    // We reached the maximum number of names. Render "et al."
                    // and then break
                    String etAl = ctx.getTerm("et-al");
                    appendDelimiter(builder, delimiterPrecedesEtAl, i, max > 1);
                    appendAnd(builder, " " + etAl);
                    break;
                }

                // render delimiter and 'and' term
                if (i == names.length - 2) {
                    boolean delimiterAppended = appendDelimiter(builder,
                            delimiterPrecedesLast, i, names.length > 2);
                    if (!delimiterAppended || !and.equals(delimiter)) {
                        appendAnd(builder, and);
                    }
                } else {
                    builder.append(delimiter);
                }
            }
        }
        ctx.emit(builder.toString());
    }

    /**
     * Append {@link #delimiter} to a builder according to the given mode
     * @param builder the builder to append to
     * @param delimiterPrecedes the mode (i.e. "contextual", "always",
     * "after-inverted-name", or "never")
     * @param i the index of the rendered name preceding the delimiter
     * @param contextual {@code true} if the delimiter should actually be
     * appended in contextual mode
     * @return {@code true} if the delimiter was actually appended
     */
    private boolean appendDelimiter(StringBuilder builder,
            String delimiterPrecedes, int i, boolean contextual) {
        boolean delimiterAppended = false;
        switch (delimiterPrecedes) {
            case "contextual":
                if (contextual) {
                    builder.append(delimiter);
                    delimiterAppended = true;
                }
                break;

            case "always":
                builder.append(delimiter);
                delimiterAppended = true;
                break;

            case "after-inverted-name":
                // IMHO, according to the standard, we should
                // check for nameAsSort == true here, but
                // citeproc.js seems to behave differently
                if (i == 0) {
                    builder.append(delimiter);
                    delimiterAppended = true;
                }
                break;

            default:
                break;
        }
        return delimiterAppended;
    }

    /**
     * Append 'and' term to builder. Ignore leading whitespace in 'and' term if
     * builder ends with a whitespace.
     * @param builder the builder to append to
     * @param and the 'and' term
     */
    private void appendAnd(StringBuilder builder, String and) {
        if (Character.isWhitespace(builder.charAt(builder.length() - 1)) &&
                Character.isWhitespace(and.charAt(0))) {
            builder.append(and, 1, and.length());
        } else {
            builder.append(and);
        }
    }

    /**
     * Render a single name
     * @param name the name to render
     * @param nameAsSort {@code true} if given name and family name should be
     * swapped (i.e. "Family, Given" instead of "Given Family")
     * @return the rendered name
     */
    private String render(CSLName name, boolean nameAsSort) {
        StringBuilder result = new StringBuilder();

        String given = name.getGiven();
        StringBuilder givenBuffer = new StringBuilder();
        if (initializeWith != null) {
            // produce initials for each given name and append
            // 'initializeWith' to each of them
            boolean found = true;
            for (int i = 0; i < given.length(); ++i) {
                char c = given.charAt(i);
                if (Character.isWhitespace(c) || c == '.') {
                    found = true;
                } else if (found) {
                    givenBuffer.append(c).append(initializeWith);
                    found = false;
                }
            }
        } else {
            givenBuffer.append(given);
        }

        given = givenBuffer.toString();
        given = StringUtils.stripEnd(given, null);

        if (nameAsSort) {
            result.append(name.getFamily()).append(sortSeparator).append(given);
        } else {
            result.append(given).append(" ").append(name.getFamily());
        }

        return result.toString();
    }
}
