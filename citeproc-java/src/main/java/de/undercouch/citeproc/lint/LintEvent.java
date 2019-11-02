package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * An event produced by {@link Linter}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintEvent {
    /**
     * Get the item data provider whose items are currently validated
     * @return the item data provider
     */
    ItemDataProvider getItemDataProvider();

    /**
     * The {@link Linter} that produced this event
     * @return the linter
     */
    Linter getSource();
}
