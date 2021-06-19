package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

/**
 * A name element from a style file
 * @author Michel Kraemer
 */
public class SName implements SElement {
    /**
     * The long form with given and family name
     */
    public final static int FORM_LONG = 0;

    /**
     * The short form with family name and non-dropping particles only
     */
    public final static int FORM_SHORT = 1;

    /**
     * A form that only returns the number of names that would be rendered
     */
    public final static int FORM_COUNT = 2;

    private final String variable;
    private final String and; // inheritable
    private final String delimiter;
    private final String delimiterPrecedesEtAl; // inheritable
    private final String delimiterPrecedesLast; // inheritable
    private final int form;
    private final boolean initialize; // inheritable
    private final String initializeWith; // inheritable
    private final String nameAsSortOrder; // inheritable
    private final String sortSeparator; // inheritable
    private final Integer etAlMin; // inheritable
    private final Integer etAlUseFirst; // inheritable
    // private final Integer etAlUseLast; // inheritable
    // private final Integer etAlUseSubsequentMin; // inheritable
    // private final Integer etAlUseSubsequentUseFirst; // inheritable
    private final int formattingAttributes;

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
        String form;
        String sortSeparator;
        String etAlMin;
        String etAlUseFirst;
        if (node != null) {
            and = NodeHelper.getAttrValue(node, "and");
            String strInitialize = NodeHelper.getAttrValue(node, "initialize");
            initialize = strInitialize == null || Boolean.parseBoolean(strInitialize);
            initializeWith = StringUtils.strip(NodeHelper.getAttrValue(node, "initialize-with"));
            nameAsSortOrder = NodeHelper.getAttrValue(node, "name-as-sort-order");
            formattingAttributes = FormattingAttributes.of(node);
            delimiter = NodeHelper.getAttrValue(node, "delimiter");
            delimiterPrecedesEtAl = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-et-al");
            delimiterPrecedesLast = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-last");
            form = NodeHelper.getAttrValue(node, "form");
            sortSeparator = NodeHelper.getAttrValue(node, "sort-separator");
            etAlMin = NodeHelper.getAttrValue(node, "et-al-min");
            etAlUseFirst = NodeHelper.getAttrValue(node, "et-al-use-first");
        } else {
            and = null;
            initialize = true;
            initializeWith = null;
            nameAsSortOrder = null;
            formattingAttributes = 0;
            delimiter = null;
            delimiterPrecedesEtAl = null;
            delimiterPrecedesLast = null;
            form = null;
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

        if ("count".equals(form)) {
            this.form = FORM_COUNT;
        } else if ("short".equals(form)) {
            this.form = FORM_SHORT;
        } else {
            this.form = FORM_LONG;
        }

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

    /**
     * Get the name variable this element selects
     * @return the name variable
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Get the form of the name to render
     * @see #FORM_LONG
     * @see #FORM_SHORT
     * @see #FORM_COUNT
     * @return the form
     */
    public int getForm() {
        return form;
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

        // shortcut: render number of names
        if (form == FORM_COUNT) {
            int count = names.length;
            if (max > -1 && max < count) {
                count = max;
            }
            ctx.emit(String.valueOf(count), formattingAttributes);
            return;
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
        ctx.emit(builder.toString(), formattingAttributes);
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
        // render family name with non-dropping particle
        String family = name.getFamily();
        if (family != null) {
            if (name.getNonDroppingParticle() != null) {
                family = name.getNonDroppingParticle().trim() + " " + family;
            }

            // render short form
            if (form == FORM_SHORT) {
                return family;
            }

            // prepend dropping particle for long form
            if (name.getDroppingParticle() != null) {
                family = name.getDroppingParticle().trim() + " " + family;
            }

            // append suffix for long form
            if (name.getSuffix() != null) {
                family = StringUtils.stripEnd(family, null) + " " + name.getSuffix().trim();
            }
        }

        // render long form
        StringBuilder result = new StringBuilder();

        String given = name.getGiven();
        StringBuilder givenBuffer = new StringBuilder();
        if (given != null) {
            if (initializeWith != null) {
                if (initialize) {
                    // produce initials for each given name and append
                    // 'initializeWith' to each of them
                    boolean found = true;
                    boolean hyphen = false;
                    for (int i = 0; i < given.length(); ++i) {
                        char c = given.charAt(i);
                        if (c == '-') {
                            found = true;
                            hyphen = true;
                        } else if (Character.isWhitespace(c) || c == '.') {
                            found = true;
                        } else if (found) {
                            if (givenBuffer.length() > 0) {
                                givenBuffer.append(hyphen ? '-' : ' ');
                            }
                            givenBuffer.append(c).append(initializeWith);
                            found = false;
                            hyphen = false;
                        }
                    }
                } else {
                    // only append 'initializeWith' to initials already present
                    StringBuilder tmp = new StringBuilder();
                    for (int i = 0; i < given.length(); ++i) {
                        char c = given.charAt(i);
                        tmp.append(c);
                        if (Character.isWhitespace(c) || c == '-' || c == '.') {
                            givenBuffer.append(tmp);
                            if (tmp.length() == 1) {
                                givenBuffer.append(initializeWith);
                            }
                            tmp = new StringBuilder();
                        }
                    }
                    if (tmp.length() > 0) {
                        givenBuffer.append(tmp);
                        if (tmp.length() == 1) {
                            givenBuffer.append(initializeWith);
                        }
                    }
                }
            } else {
                givenBuffer.append(given);
            }
        }

        if (nameAsSort) {
            if (family != null) {
                result.append(family);
            }
            if (family != null && given != null) {
                result.append(sortSeparator);
            }
            if (given != null) {
                result.append(givenBuffer);
            }
        } else {
            if (given != null) {
                result.append(givenBuffer);
            }
            if (given != null && family != null) {
                result.append(" ");
            }
            if (family != null) {
                result.append(family);
            }
        }

        return result.toString();
    }
}
