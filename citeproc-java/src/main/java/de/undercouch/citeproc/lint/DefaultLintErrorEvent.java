package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * Default implementation of {@link LintErrorEvent}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLintErrorEvent extends DefaultLintItemEvent
        implements LintErrorEvent {
    private final Type type;
    private final String message;

    /**
     * Construct a new error event
     * @param type the error type
     * @param message the error message
     * @param citationId the ID of the citation considered erroneous
     * @param itemDataProvider the item data provider that provided the
     * erroneous citation item
     * @param source the {@link Linter} that produced this event
     */
    public DefaultLintErrorEvent(Type type, String message, String citationId,
            ItemDataProvider itemDataProvider, Linter source) {
        super(citationId, itemDataProvider, source);
        this.type = type;
        this.message = message;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
