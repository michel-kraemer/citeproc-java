package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.behavior.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final List<Formatting> formatting;

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     * @param formatting the token's formatting attributes (may be {@code null})
     */
    private Token(String text, Type type, List<Formatting> formatting) {
        this.text = text;
        this.type = type;
        this.formatting = formatting;
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
     * Get an unmodifiable list of the token's formatting attributes
     * @return the formatting attributes (may be {@code null})
     */
    public List<Formatting> getFormatting() {
        if (formatting == null) {
            return null;
        }
        return Collections.unmodifiableList(formatting);
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
        private List<Formatting> formatting;
        private boolean formattingModifiable = false;
        private boolean built = false;

        /**
         * Default constructor
         */
        public Builder() {
            // nothing to do here
        }

        /**
         * Creates a builder that copies the attributes of the given token
         * @param token the token to copy
         */
        public Builder(Token token) {
            this.text = token.text;
            this.type = token.type;
            this.formatting = token.formatting;
        }

        /**
         * Sets the token's text
         * @param text the text
         * @return this builder
         */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the token's type
         * @param type the type
         * @return this builder
         */
        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the token's formatting attributes
         * @param formatting the formatting attributes
         * @return this builder
         */
        public Builder formatting(List<Formatting> formatting) {
            if (formatting == null) {
                this.formatting = null;
                formattingModifiable = false;
            } else if (formatting.size() == 1) {
                this.formatting = Collections.singletonList(formatting.get(0));
                formattingModifiable = false;
            } else {
                this.formatting = new ArrayList<>(formatting);
                formattingModifiable = true;
            }
            return this;
        }

        /**
         * Appends formatting attributes
         * @param formatting the formatting attributes to append to the token
         * @return this builder
         */
        public Builder appendFormatting(Formatting formatting) {
            if (formatting == null) {
                return this;
            }

            if (!formattingModifiable) {
                if (this.formatting == null) {
                    this.formatting = Collections.singletonList(formatting);
                } else {
                    this.formatting = new ArrayList<>(this.formatting);
                    this.formatting.add(formatting);
                    formattingModifiable = true;
                }
            } else {
                this.formatting.add(formatting);
            }

            return this;
        }

        /**
         * Build the token
         * @return the token
         */
        public Token build() {
            if (built) {
                throw new IllegalStateException("A token builder may only " +
                        "be used once.");
            }
            built = true;
            return new Token(text, type, formatting);
        }
    }
}
