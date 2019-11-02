package de.undercouch.citeproc.lint;

/**
 * An event produced by {@link Linter} related to a specific
 * citation item
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintItemEvent extends LintEvent {
    /**
     * Get the ID of the citation item this event is related to
     * @return the citation ID
     */
    String getCitationId();
}
