package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * Default implementation of {@link LintEvent}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLintEvent implements LintEvent {
    private final ItemDataProvider itemDataProvider;
    private final Linter source;

    /**
     * Constructs a new event
     * @param itemDataProvider the item data provider whose items are
     * currently validated
     * @param source the {@link Linter} that produced this event
     */
    public DefaultLintEvent(ItemDataProvider itemDataProvider, Linter source) {
        this.itemDataProvider = itemDataProvider;
        this.source = source;
    }

    @Override
    public ItemDataProvider getItemDataProvider() {
        return itemDataProvider;
    }

    @Override
    public Linter getSource() {
        return source;
    }
}
