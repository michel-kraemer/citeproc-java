package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.helper.IntBuffer;

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
        DELIMITER
    }

    private final String text;
    private final Type type;
    private final IntBuffer formattingAttributes;

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes (may be
     * {@code null})
     */
    private Token(String text, Type type, IntBuffer formattingAttributes) {
        this.text = text;
        this.type = type;
        this.formattingAttributes = formattingAttributes;
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
     * @return the formatting attributes (may be {@code null})
     */
    public IntBuffer getFormattingAttributes() {
        return formattingAttributes;
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
        private IntBuffer formattingAttributes;

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
         * Set the token's formatting attributes
         * @param formattingAttributes the formatting attributes
         * @return this builder
         */
        public Builder formattingAttributes(IntBuffer formattingAttributes) {
            this.formattingAttributes = formattingAttributes;
            return this;
        }

        /**
         * Append formatting attributes
         * @param formattingAttributes the formatting attributes to append to
         * the token
         * @return this builder
         */
        public Builder appendFormattingAttributes(int formattingAttributes) {
            if (this.formattingAttributes == null) {
                this.formattingAttributes = new IntBuffer(formattingAttributes);
            } else {
                this.formattingAttributes = this.formattingAttributes.append(
                        formattingAttributes);
            }
            return this;
        }

        /**
         * Build the token
         * @return the token
         */
        public Token build() {
            return new Token(text, type, formattingAttributes);
        }
    }
}
