package de.undercouch.citeproc.csl.internal.token;

/**
 * A rendered token
 * @author Michel Kraemer
 */
public abstract class Token {
    private final int formattingAttributes;
    private final boolean firstField;

    /**
     * Construct a new token
     * @param formattingAttributes the token's formatting attributes
     * @param firstField {@code true} if the token is part of the first
     * rendered field in a bibliography entry
     */
    public Token(int formattingAttributes, boolean firstField) {
        this.formattingAttributes = formattingAttributes;
        this.firstField = firstField;
    }

    /**
     * Get the token's formatting attributes
     * @return the formatting attributes
     */
    public int getFormattingAttributes() {
        return formattingAttributes;
    }

    /**
     * Return {@code true} if the token is part of the first rendered field in
     * a bibliography entry
     * @return {@code true} if the token is part of the first field
     */
    public boolean isFirstField() {
        return firstField;
    }

    /**
     * Wrap other formatting attributes around the formatting attributes of
     * this token and return a new token. The formatting attributes of this
     * token will be merged into the other attributes, so that attributes that
     * are set in this token overwrite the other attributes.
     * @param otherFormattingAttributes the other formatting attributes
     * @return the new token with wrapped formatting attributes
     */
    public abstract Token wrapFormattingAttributes(int otherFormattingAttributes);

    /**
     * Create a copy of this token but replaces its first-field flag
     * @param firstField the new first-field flag
     * @return the copied token
     */
    public abstract Token copyWithFirstField(boolean firstField);
}
