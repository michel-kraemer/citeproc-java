package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLName;

/**
 * A variable listener that counts the number of variables fetched from
 * the {@link RenderContext}.
 * @author Michel Kraemer
 */
public class CountingVariableListener implements VariableListener {
    private int called = 0;
    private int empty = 0;

    @Override
    public void onFetchStringVariable(String name, String value) {
        called++;
        if (value == null || value.isEmpty()) {
            empty++;
        }
    }

    @Override
    public void onFetchNameVariable(String name, CSLName[] value) {
        called++;
        if (value == null) {
            empty++;
        }
    }

    @Override
    public void onFetchDateVariable(String name, CSLDate value) {
        called++;
        if (value == null) {
            empty++;
        }
    }

    /**
     * Get the number of times a variable was fetched from the context
     * @return the number of fetched variables
     */
    public int getCalled() {
        return called;
    }

    /**
     * Get the number of times an empty variable was fetched from the context.
     * This value is always lower than or equal to the one returned by
     * {@link #getCalled()}.
     * @return the number of empty variables fetched from the context
     */
    public int getEmpty() {
        return empty;
    }
}
