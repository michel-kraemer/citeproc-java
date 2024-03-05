package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.internal.token.TextToken;
import de.undercouch.citeproc.csl.internal.token.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A buffer of rendered tokens
 * @author Michel Kraemer
 */
public class TokenBuffer {
    private final List<Token> tokens;

    /**
     * Create an empty token buffer
     */
    public TokenBuffer() {
        tokens = new ArrayList<>();
    }

    /**
     * Create a token buffer with the given list of tokens
     * @param tokens the token list
     */
    private TokenBuffer(List<Token> tokens) {
        this.tokens = tokens;
    }

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
     * Prepend a token to the buffer
     * @param token the token
     * @return this token buffer
     */
    public TokenBuffer prepend(Token token) {
        tokens.add(0, token);
        return this;
    }

    /**
     * Append a text token with a given type to the buffer
     * @param text the token's text
     * @param type the token's type
     * @return this token buffer
     */
    public TokenBuffer append(String text, TextToken.Type type) {
        return append(new TextToken(text, type));
    }

    /**
     * Prepend a text token with a given type to the buffer
     * @param text the token's text
     * @param type the token's type
     * @return this token buffer
     */
    public TokenBuffer prepend(String text, TextToken.Type type) {
        return prepend(new TextToken(text, type));
    }

    /**
     * Append a text token with a given type and formatting attributes to
     * the buffer
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @return this token buffer
     */
    public TokenBuffer append(String text, TextToken.Type type,
            int formattingAttributes) {
        return append(new TextToken(text, type, formattingAttributes));
    }

    /**
     * Prepend a text token with a given type and formatting attributes to
     * the buffer
     * @param text the token's text
     * @param type the token's type
     * @param formattingAttributes the token's formatting attributes
     * @return this token buffer
     */
    public TokenBuffer prepend(String text, TextToken.Type type,
            int formattingAttributes) {
        return prepend(new TextToken(text, type, formattingAttributes));
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
     * Prepend the contents of another token buffer to this one
     * @param other the other token buffer
     * @return this token buffer
     */
    public TokenBuffer prepend(TokenBuffer other) {
        this.tokens.addAll(0, other.tokens);
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

    /**
     * Create a new token buffer that contains a portion of this one between
     * the specified {@code fromIndex} and {@code toIndex} (exclusive).
     * @param fromIndex the index of the first element (inclusive)
     * @param toIndex the index of the last element (exclusive)
     * @return the new token buffer
     */
    public TokenBuffer copy(int fromIndex, int toIndex) {
        return new TokenBuffer(tokens.subList(fromIndex, toIndex));
    }

    @Override
    public String toString() {
        return tokens.stream().map(Token::toString).collect(Collectors.joining());
    }
}
