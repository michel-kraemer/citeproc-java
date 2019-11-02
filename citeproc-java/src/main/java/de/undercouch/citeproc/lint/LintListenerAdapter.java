package de.undercouch.citeproc.lint;

/**
 * A default implementation of {@link LintListener}. The methods in this class
 * are empty and can be overridden for more specific listener implementations.
 * @author Michel Kraemer
 * @since 1.1.0
 */
public abstract class LintListenerAdapter implements LintListener {
    @Override
    public void onStart(LintEvent e) {
        // nothing to do here
    }

    @Override
    public void onEnd(LintEvent e) {
        // nothing to do here
    }

    @Override
    public void onStartItem(LintItemEvent e) {
        // nothing to do here
    }

    @Override
    public void onEndItem(LintItemEvent e) {
        // nothing to do here
    }

    @Override
    public void onError(LintErrorEvent e) {
        // nothing to do here
    }
}
