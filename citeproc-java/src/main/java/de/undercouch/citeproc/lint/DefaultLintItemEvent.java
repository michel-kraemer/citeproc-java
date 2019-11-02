package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * Default implementation of {@link LintItemEvent}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLintItemEvent extends DefaultLintEvent implements LintItemEvent {
    private final String citationId;

    /**
     * Construct a new error event
     * @param citationId the ID of the citation considered erroneous
     * @param itemDataProvider the item data provider that provided the
     * erroneous citation item
     * @param source the {@link Linter} that produced this event
     */
    public DefaultLintItemEvent(String citationId,
            ItemDataProvider itemDataProvider, Linter source) {
        super(itemDataProvider, source);
        this.citationId = citationId;
    }

    @Override
    public String getCitationId() {
        return citationId;
    }
}
