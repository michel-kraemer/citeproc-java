package de.undercouch.citeproc.csl.internal.token;

import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;

/**
 * Base class for all text tokens
 * @author Michel Kraemer
 */
public class TextToken extends Token {
    /**
     * The token's type
     */
    public enum Type {
        /**
         * A simple text token
         */
        TEXT,

        /**
         * A token that represents an opening quotation mark
         */
        OPEN_QUOTE,

        /**
         * A token that represents a closing quotation mark
         */
        CLOSE_QUOTE,

        /**
         * A token that represents a prefix
         */
        PREFIX,

        /**
         * A token that represents a suffix
         */
        SUFFIX,

        /**
         * A token that represents a delimiter
         */
        DELIMITER,

        /**
         * A token that represents a URL
         */
        URL,

        /**
         * A token that represents a DOI
         */
        DOI
    }

    private final String text;
    private final Type type;

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     */
    public TextToken(String text, Type type) {
        this(text, type, 0);
    }

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     */
    public TextToken(String text, Type type, int formattingAttributes) {
        this(text, type, formattingAttributes, false);
    }

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @param firstField {@code true} if the token is part of the first
     * rendered field in a bibliography entry
     */
    public TextToken(String text, Type type, int formattingAttributes,
            boolean firstField) {
        super(formattingAttributes, firstField);
        this.text = text;
        this.type = type;
    }

    /**
     * Get the token's text
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Get the token's type
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * Creates a copy of this token but replaces its text
     * @param text the new text
     * @return the copied token with new text
     */
    public TextToken copyWithText(String text) {
        return new TextToken(text, this.getType(),
                this.getFormattingAttributes(), this.isFirstField());
    }

    @Override
    public TextToken copyWithFirstField(boolean firstField) {
        return new TextToken(this.getText(), this.getType(),
                this.getFormattingAttributes(), firstField);
    }

    @Override
    public Token wrapFormattingAttributes(int otherFormattingAttributes) {
        int fa = FormattingAttributes.merge(otherFormattingAttributes,
                this.getFormattingAttributes());
        return new TextToken(this.getText(), this.getType(), fa,
                this.isFirstField());
    }
}
