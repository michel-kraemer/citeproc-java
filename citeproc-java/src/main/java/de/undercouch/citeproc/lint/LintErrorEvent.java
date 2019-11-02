package de.undercouch.citeproc.lint;

/**
 * An event produced by {@link Linter} when an erroneous
 * citation item was found
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintErrorEvent extends LintItemEvent {
    /**
     * Error types
     */
    enum Type {
        /**
         * The linter found a duplicate citation ID
         */
        DUPLICATE_ID
    }

    /**
     * Get the error type
     * @return the type
     */
    Type getType();

    /**
     * Get the error message
     * @return the message
     */
    String getMessage();
}

