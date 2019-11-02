package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * A linter validates citation items from an {@link ItemDataProvider} and
 * notifies a {@link LintListener} about the results
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface Linter {
    /**
     * Validate citation items provided by the given {@link ItemDataProvider}
     * @param provider the item data provider
     */
    void lint(ItemDataProvider provider);

    /**
     * Validate a single citation item
     * @param item the item to validate
     * @param provider the item data provider that provided the citation item
     */
    void lintItem(CSLItemData item, ItemDataProvider provider);

    /**
     * Add a new listener that will receive events with validation results
     * @param listener the listener to add
     */
    void addListener(LintListener listener);

    /**
     * Remove a listener
     * @param listener the listener to remove
     */
    void removeListener(LintListener listener);

    /**
     * Add a filter that will decide which citation items should be validated
     * and which should not
     * @param filter the filter to add
     */
    void addFilter(LintFilter filter);

    /**
     * Remove a filter
     * @param filter the filter to remove
     */
    void removeFilter(LintFilter filter);
}
