package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SElement;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.Affixes;
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;
import de.undercouch.citeproc.csl.internal.behavior.TextCase;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.StringHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.DELIMITER;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.SUFFIX;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.TEXT;

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
    private final int familyFormattingAttributes;
    private final int givenFormattingAttributes;
    private final Affixes familyAffixes;
    private final Affixes givenAffixes;
    private final TextCase familyTextCase;
    private final TextCase givenTextCase;

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
        boolean namePartFamilyParsed = false;
        boolean namePartGivenParsed = false;
        int familyFormattingAttributes = FormattingAttributes.UNDEFINED;
        int givenFormattingAttributes = FormattingAttributes.UNDEFINED;
        Affixes familyAffixes = new Affixes();
        Affixes givenAffixes = new Affixes();
        TextCase familyTextCase = null;
        TextCase givenTextCase = null;
        if (node != null) {
            formattingAttributes = FormattingAttributes.of(node);
            delimiter = NodeHelper.getAttrValue(node, "delimiter");
            form = NodeHelper.getAttrValue(node, "form");

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                Node c = children.item(i);
                String nodeName = c.getNodeName();
                if ("name-part".equals(nodeName)) {
                    String namePartName = NodeHelper.getAttrValue(c, "name");
                    if (namePartName == null) {
                        throw new IllegalStateException("Missing name part name");
                    }
                    if (namePartName.equals("family")) {
                        if (!namePartFamilyParsed) {
                            familyFormattingAttributes = FormattingAttributes.of(c);
                            familyAffixes = new Affixes(c);
                            familyTextCase = new TextCase(c);
                            namePartFamilyParsed = true;
                        } else {
                            throw new IllegalStateException("Duplicate name part name: family");
                        }
                    } else if (namePartName.equals("given")) {
                        if (!namePartGivenParsed) {
                            givenFormattingAttributes = FormattingAttributes.of(c);
                            givenAffixes = new Affixes(c);
                            givenTextCase = new TextCase(c);
                            namePartGivenParsed = true;
                        } else {
                            throw new IllegalStateException("Duplicate name part name: given");
                        }
                    } else {
                        throw new IllegalStateException("Unknown name part name: " + namePartName);
                    }
                }
            }
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

        this.familyFormattingAttributes = familyFormattingAttributes;
        this.givenFormattingAttributes = givenFormattingAttributes;
        this.familyAffixes = familyAffixes;
        this.givenAffixes = givenAffixes;
        this.familyTextCase = familyTextCase;
        this.givenTextCase = givenTextCase;
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

        TokenBuffer buffer = new TokenBuffer();
        for (int i = 0; i < names.size(); ++i) {
            boolean nameAsSort = "all".equals(nameAsSortOrder) ||
                    (i == 0 && "first".equals(nameAsSortOrder));
            buffer.append(render(names.get(i), nameAsSort, initializeWith,
                    initialize, sortSeparator, ctx));

            if (i < names.size() - 1) {
                if (i == max - 1) {
                    // We reached the maximum number of names. Render "et al."
                    // and then break
                    String etAl = ctx.getTerm("et-al");
                    appendDelimiter(buffer, delimiterPrecedesEtAl, nameAsSort, max > 1);
                    buffer.append(" " + etAl, SUFFIX);
                    break;
                }

                // render delimiter and 'and' term
                if (i == names.size() - 2) {
                    boolean delimiterAppended = appendDelimiter(buffer,
                            delimiterPrecedesLast, nameAsSort, names.size() > 2);
                    if (!delimiterAppended || !renderedAnd.equals(delimiter)) {
                        buffer.append(renderedAnd, DELIMITER);
                    }
                } else {
                    buffer.append(delimiter, DELIMITER);
                }
            }
        }
        ctx.emit(buffer, formattingAttributes);
    }

    /**
     * Append {@link #delimiter} to a builder according to the given mode
     * @param buffer the token buffer to append to
     * @param delimiterPrecedes the mode (i.e. "contextual", "always",
     * "after-inverted-name", or "never")
     * @param nameAsSort {@code true} if the name preceding the delimiter
     * has been inverted as a result of the {@code name-as-sort-order} attribute
     * @param contextual {@code true} if the delimiter should actually be
     * appended in contextual mode
     * @return {@code true} if the delimiter was actually appended
     */
    private boolean appendDelimiter(TokenBuffer buffer,
            String delimiterPrecedes, boolean nameAsSort, boolean contextual) {
        boolean delimiterAppended = false;
        switch (delimiterPrecedes) {
            case "contextual":
                if (contextual) {
                    buffer.append(delimiter, DELIMITER);
                    delimiterAppended = true;
                }
                break;

            case "always":
                buffer.append(delimiter, DELIMITER);
                delimiterAppended = true;
                break;

            case "after-inverted-name":
                // side note: IMHO, according to the standard, we must
                // check for nameAsSort == true here, but citeproc.js
                // seems to behave differently
                if (nameAsSort) {
                    buffer.append(delimiter, DELIMITER);
                    delimiterAppended = true;
                }
                break;

            default:
                break;
        }
        return delimiterAppended;
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
     * @param ctx the current render context
     * @return the rendered name
     */
    private TokenBuffer render(CSLName name, boolean nameAsSort, String initializeWith,
            boolean initialize, String sortSeparator, RenderContext ctx) {
        // if the 'literal' attribute is set, just return its value and nothing else
        if (name.getLiteral() != null) {
            return new TokenBuffer().append(name.getLiteral(), TEXT);
        }

        // render family name with non-dropping particle
        String strFamily = name.getFamily();
        TokenBuffer family = new TokenBuffer();
        if (strFamily != null) {
            if (familyTextCase != null) {
                strFamily = familyTextCase.applyTo(strFamily, ctx);
            }
            family.append(strFamily, TEXT, familyFormattingAttributes);

            if (name.getNonDroppingParticle() != null) {
                family.prepend(" ", DELIMITER);
                String ndp = name.getNonDroppingParticle().trim();
                if (familyTextCase != null) {
                    ndp = familyTextCase.applyTo(ndp, ctx);
                }
                family.prepend(ndp, TEXT, familyFormattingAttributes);
            }

            // render short form
            if (form == FORM_SHORT) {
                return family;
            }

            // prepend dropping particle for long form
            if (name.getDroppingParticle() != null) {
                family.prepend(" ", DELIMITER);
                // according to the spec, we need to use the formatting
                // attributes of the given name here
                String dp = name.getDroppingParticle().trim();
                if (givenTextCase != null) {
                    dp = givenTextCase.applyTo(dp, ctx);
                }
                family.prepend(dp, TEXT, givenFormattingAttributes);
            }

            // append suffix for long form
            if (name.getSuffix() != null) {
                family.append(" ", DELIMITER);
                family.append(name.getSuffix().trim(), TEXT);
            }
        }
        familyAffixes.applyTo(family);

        // render long form
        String strGiven = name.getGiven();
        if (initializeWith != null && !family.isEmpty() && strGiven != null) {
            strGiven = StringHelper.initializeName(strGiven, initializeWith, !initialize);
        }
        TokenBuffer given = new TokenBuffer();
        if (strGiven != null) {
            if (givenTextCase != null) {
                strGiven = givenTextCase.applyTo(strGiven, ctx);
            }
            given.append(strGiven, TEXT, givenFormattingAttributes);
        }
        givenAffixes.applyTo(given);

        TokenBuffer result = new TokenBuffer();
        if (nameAsSort) {
            result.append(family);
            if (!family.isEmpty() && !given.isEmpty()) {
                result.append(sortSeparator, DELIMITER);
            }
            result.append(given);
        } else {
            result.append(given);
            if (!given.isEmpty() && !family.isEmpty()) {
                result.append(" ", DELIMITER);
            }
            result.append(family);
        }

        return result;
    }
}
