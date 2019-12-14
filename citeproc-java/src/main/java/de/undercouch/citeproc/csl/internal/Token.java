package de.undercouch.citeproc.csl.internal;

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
        SUFFIX
    }

    private final String text;
    private final Type type;

    /**
     * Construct a new token
     * @param text the token's text
     * @param type the token's type
     */
    public Token(String text, Type type) {
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
}
