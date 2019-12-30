package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;

/**
 * A rendered token
 * @author Michel Kraemer
 */
public class Token {
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
    private final int formattingAttributes;
    private final boolean firstField;

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @param firstField {@code true} if the token is part of the first
     * rendered field in a bibliography entry
     */
    private Token(String text, Type type, int formattingAttributes,
            boolean firstField) {
        this.text = text;
        this.type = type;
        this.formattingAttributes = formattingAttributes;
        this.firstField = firstField;
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

    @Override
    public String toString() {
        return text;
    }

    /**
     * A builder for {@link Token} objects
     */
    public static class Builder {
        private String text;
        private Type type;
        private int formattingAttributes;
        private boolean firstField;

        /**
         * Default constructor
         */
        public Builder() {
            // nothing to do here
        }

        /**
         * Create a builder that copies the attributes of the given token
         * @param token the token to copy
         */
        public Builder(Token token) {
            this.text = token.text;
            this.type = token.type;
            this.formattingAttributes = token.formattingAttributes;
            this.firstField = token.firstField;
        }

        /**
         * Set the token's text
         * @param text the text
         * @return this builder
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Set the token's type
         * @param type the type
         * @return this builder
         */
        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        /**
         * Merge formatting attributes with the current set of formatting
         * attributes. An attribute from the given set only will only be merged
         * if the respective current attribute is undefined.
         * @param formattingAttributes the formatting attributes to merge with
         * the current set of formatting attributes
         * @return this builder
         */
        public Builder mergeFormattingAttributes(int formattingAttributes) {
            this.formattingAttributes = FormattingAttributes.merge(
                    formattingAttributes, this.formattingAttributes);
            return this;
        }

        /**
         * Specify that the token is part of the first rendered field in a
         * bibliography entry
         * @param firstField {@code true} if the token is part of the first field
         * @return this builder
         */
        public Builder firstField(boolean firstField) {
            this.firstField = firstField;
            return this;
        }

        /**
         * Build the token
         * @return the token
         */
        public Token build() {
            return new Token(text, type, formattingAttributes, firstField);
        }
    }
}
