package de.undercouch.citeproc.csl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A buffer of rendered tokens
 * @author Michel Kraemer
 */
public class TokenBuffer {
    private final List<Token> tokens = new ArrayList<>();

    /**
     * Append a token to the buffer
     * @param token the token
     * @return this token buffer
     */
    public TokenBuffer append(Token token) {
        tokens.add(token);
        return this;
    }

    /**
     * Append a token with a given type to the buffer
     * @param text the token's text
     * @param type the token's type
     * @return this token buffer
     */
    public TokenBuffer append(String text, Token.Type type) {
        return append(new Token.Builder()
                .text(text)
                .type(type)
                .build());
    }

    /**
     * Append a token with a given type and formatting attributes to the buffer
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @return this token buffer
     */
    public TokenBuffer append(String text, Token.Type type, int formattingAttributes) {
        return append(new Token.Builder()
                .text(text)
                .type(type)
                .mergeFormattingAttributes(formattingAttributes)
                .build());
    }

    /**
     * Append the contents of another token buffer to this one
     * @param other the other token buffer
     * @return this token buffer
     */
    public TokenBuffer append(TokenBuffer other) {
        this.tokens.addAll(other.tokens);
        return this;
    }

    /**
     * Get the list of tokens managed by this buffer
     * @return the list
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Test if the buffer is empty
     * @return {@code true} if the buffer does not contain tokens
     */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    @Override
    public String toString() {
        return tokens.stream().map(Token::toString).collect(Collectors.joining());
    }
}
