package de.undercouch.citeproc.lint;

/**
 * A listener that will receive events from a {@link Linter}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintListener {
    /**
     * Will be called when the linter has started validating citation
     * items from an item data provider
     * @param e an event containing additional information
     */
    void onStart(LintEvent e);

    /**
     * Will be called when the linter has finished validating
     * @param e an event containing additional information
     */
    void onEnd(LintEvent e);

    /**
     * Will be called when the linter has started validating a
     * specific citation item
     * @param e an event containing additional information
     */
    void onStartItem(LintItemEvent e);

    /**
     * Will be called when the linter has finished validating a
     * specific citation item
     * @param e an event containing additional information
     */
    void onEndItem(LintItemEvent e);

    /**
     * Will be called when the linter has found an error in one
     * of the validated citation items
     * @param e an event containing additional information about the
     * validation error
     */
    void onError(LintErrorEvent e);
}
