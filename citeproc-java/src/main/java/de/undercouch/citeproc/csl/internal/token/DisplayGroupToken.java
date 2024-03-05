package de.undercouch.citeproc.csl.internal.token;

import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes;

/**
 * A token that controls how text blocks should be displayed
 * @author Michel Kraemer
 */
public class DisplayGroupToken extends Token {
    /**
     * The token's type
     */
    public enum Type {
        /**
         * The group should be displayed as a text block
         */
        BLOCK,

        /**
         * The group is a text block that should start at the left margin
         */
        LEFT_MARGIN,

        /**
         * The group represents a text block starting to the right of a
         * preceding left-margin block
         */
        RIGHT_INLINE,

        /**
         * The group is a text block indented to the right
         */
        INDENT
    }

    private final boolean open;
    private final Type type;

    /**
     * Construct a new token
     * @param open {@code true} if the token opens a new display group,
     * {@code false} if it closes it
     * @param type the token's type
     */
    public DisplayGroupToken(boolean open, Type type) {
        this(open, type, 0);
    }

    /**
     * Construct a new token
     * @param open {@code true} if the token opens a new display group,
     * {@code false} if it closes it
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     */
    public DisplayGroupToken(boolean open, Type type, int formattingAttributes) {
        this(open, type, formattingAttributes, false);
    }

    /**
     * Construct a new token
     * @param open {@code true} if the token opens a new display group,
     * {@code false} if it closes it
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @param firstField {@code true} if the token is part of the first
     */
    public DisplayGroupToken(boolean open, Type type, int formattingAttributes,
            boolean firstField) {
        super(formattingAttributes, firstField);
        this.open = open;
        this.type = type;
    }

    /**
     * Get whether the token opens a display group ({@code true}) or if it
     * closes it ({@code false})
     * @return the {@code true} if the token opens a new display group,
     * {@code false} if it closes it
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Get the token's type
     * @return the type
     */
    public Type getType() {
        return type;
    }

    @Override
    public DisplayGroupToken copyWithFirstField(boolean firstField) {
        return new DisplayGroupToken(this.isOpen(), this.getType(),
                this.getFormattingAttributes(), firstField);
    }

    @Override
    public Token wrapFormattingAttributes(int otherFormattingAttributes) {
        int fa = FormattingAttributes.merge(otherFormattingAttributes,
                this.getFormattingAttributes());
        return new DisplayGroupToken(this.isOpen(), this.getType(), fa,
                this.isFirstField());
    }
}
