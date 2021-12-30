package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.StringHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final String delimiter;
    private final int form;
    private final int formattingAttributes;
    private final SNameInheritableAttributes inheritableAttributes;

    /**
     * Create the name element from an XML node
     * @param node the XML node (may be {@code null})
     * @param variable the variable that holds the name (may be {@code null})
     */
    public SName(Node node, String variable) {
        this.variable = variable;
        this.inheritableAttributes = new SNameInheritableAttributes(node);

        String delimiter;
        String form;
        if (node != null) {
            formattingAttributes = FormattingAttributes.of(node);
            delimiter = NodeHelper.getAttrValue(node, "delimiter");
            form = NodeHelper.getAttrValue(node, "form");
        } else {
            formattingAttributes = 0;
            delimiter = null;
            form = null;
        }

        if (delimiter == null) {
            delimiter = ", ";
        }
        this.delimiter = delimiter;

        if ("count".equals(form)) {
            this.form = FORM_COUNT;
        } else if ("short".equals(form)) {
            this.form = FORM_SHORT;
        } else {
            this.form = FORM_LONG;
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
        List<CSLName> names = new ArrayList<>();
        if (variable != null) {
            String[] variables = variable.split("\\s+");
            for (String v : variables) {
                CSLName[] vn = ctx.getNameVariable(v);
                if (vn != null) {
                    names.addAll(Arrays.asList(vn));
                }
            }
        }
        if (names.isEmpty()) {
            return;
        }

        SNameInheritableAttributes ia = ctx.getInheritedNameAttributes().merge(inheritableAttributes);
        String and = ia.getAnd();
        Integer etAlMin = ia.getEtAlMin();
        Integer etAlUseFirst = ia.getEtAlUseFirst();
        String nameAsSortOrder = ia.getNameAsSortOrder();
        String delimiterPrecedesEtAl = ia.getDelimiterPrecedesEtAl();
        String delimiterPrecedesLast = ia.getDelimiterPrecedesLast();
        String initializeWith = ia.getInitializeWith();
        boolean initialize = ia.isInitialize();
        String sortSeparator = ia.getSortSeparator();

        String renderedAnd;
        if ("text".equals(and)) {
            renderedAnd = " " + ctx.getTerm("and") + " ";
        } else if ("symbol".equals(and)) {
            renderedAnd = " & ";
        } else {
            renderedAnd = delimiter;
        }

        // get the maximum number of names to render before "et al."
        int max = -1;
        if (etAlMin != null && etAlUseFirst != null && names.size() >= etAlMin) {
            max = etAlUseFirst;
            if (max == 0) {
                // do not render any name
                return;
            }
        }

        // shortcut: render number of names
        if (form == FORM_COUNT) {
            int count = names.size();
            if (max > -1 && max < count) {
                count = max;
            }
            ctx.emit(String.valueOf(count), formattingAttributes);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < names.size(); ++i) {
            boolean nameAsSort = "all".equals(nameAsSortOrder) ||
                    (i == 0 && "first".equals(nameAsSortOrder));
            builder.append(render(names.get(i), nameAsSort, initializeWith,
                    initialize, sortSeparator));

            if (i < names.size() - 1) {
                if (i == max - 1) {
                    // We reached the maximum number of names. Render "et al."
                    // and then break
                    String etAl = ctx.getTerm("et-al");
                    appendDelimiter(builder, delimiterPrecedesEtAl, nameAsSort, max > 1);
                    appendAnd(builder, " " + etAl);
                    break;
                }

                // render delimiter and 'and' term
                if (i == names.size() - 2) {
                    boolean delimiterAppended = appendDelimiter(builder,
                            delimiterPrecedesLast, nameAsSort, names.size() > 2);
                    if (!delimiterAppended || !renderedAnd.equals(delimiter)) {
                        appendAnd(builder, renderedAnd);
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
     * @param nameAsSort {@code true} if the name preceding the delimiter
     * has been inverted as a result of the {@code name-as-sort-order} attribute
     * @param contextual {@code true} if the delimiter should actually be
     * appended in contextual mode
     * @return {@code true} if the delimiter was actually appended
     */
    private boolean appendDelimiter(StringBuilder builder,
            String delimiterPrecedes, boolean nameAsSort, boolean contextual) {
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
                // side note: IMHO, according to the standard, we must
                // check for nameAsSort == true here, but citeproc.js
                // seems to behave differently
                if (nameAsSort) {
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
        if (builder.length() > 0 &&
                Character.isWhitespace(builder.charAt(builder.length() - 1)) &&
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
     * @param initializeWith the string to append to initials
     * @param initialize {@code true} if given names should be converted to initials
     * @param sortSeparator delimiter for name-parts that have switched positions
     * as a result of {@code name-as-sort-order}
     * @return the rendered name
     */
    private String render(CSLName name, boolean nameAsSort, String initializeWith,
            boolean initialize, String sortSeparator) {
        // if the 'literal' attribute is set, just return its value and nothing else
        if (name.getLiteral() != null) {
            return name.getLiteral();
        }

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
        if (initializeWith != null && family != null && given != null) {
            given = StringHelper.initializeName(given, initializeWith, !initialize);
        }

        if (nameAsSort) {
            if (family != null) {
                result.append(family);
            }
            if (family != null && given != null) {
                result.append(sortSeparator);
            }
            if (given != null) {
                result.append(given);
            }
        } else {
            if (given != null) {
                result.append(given);
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
