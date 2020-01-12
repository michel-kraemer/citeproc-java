package de.undercouch.citeproc.csl.internal.helper;

import de.undercouch.citeproc.csl.CSLLabel;

import java.util.Objects;

/**
 * A result of {@link NumberParser#parse(String)}. Contains a string with an
 * optional label as well as a flag specifying whether the string contains
 * multiple numbers or a range (i.e. if the plural form of the label should be
 * selected) or if it does not.
 */
public class NumberElement {
    private final String text;
    private final CSLLabel label;
    private final boolean plural;

    /**
     * Construct the element without a label
     * @param text the element's string
     */
    public NumberElement(String text) {
        this(text, null, false);
    }

    /**
     * Construct the element
     * @param text the element's string
     * @param label the element's label (may be {@code null})
     * @param plural specifies whether the string contains multiple numbers
     * or a range (i.e. if the plural form of the label should be selected)
     * or if it does not.
     */
    public NumberElement(String text, CSLLabel label, boolean plural) {
        this.text = text;
        this.label = label;
        this.plural = plural;
    }

    /**
     * Get the element's text
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the element's label
     * @return the label
     */
    public CSLLabel getLabel() {
        return label;
    }

    /**
     * Determine whether the string contains multiple numbers or a range (i.e.
     * if the plural form of the label should be selected) or if it does not.
     * @return {@code true} if the string contains multiple numbers
     */
    public boolean isPlural() {
        return plural;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NumberElement that = (NumberElement)o;
        return plural == that.plural &&
                text.equals(that.text) &&
                label == that.label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, label, plural);
    }
}
