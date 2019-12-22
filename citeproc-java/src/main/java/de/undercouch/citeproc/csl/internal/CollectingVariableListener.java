package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLName;

import java.util.HashSet;
import java.util.Set;

/**
 * A variable listener that collects the names of all variables fetched from
 * the render context
 * @author Michel Kraemer
 */
public class CollectingVariableListener implements VariableListener {
    private final Set<String> called = new HashSet<>();

    @Override
    public void onFetchStringVariable(String name, String value) {
        called.add(name);
    }

    @Override
    public void onFetchNameVariable(String name, CSLName[] value) {
        called.add(name);
    }

    @Override
    public void onFetchDateVariable(String name, CSLDate value) {
        called.add(name);
    }

    /**
     * Get the set of variables fetched from the render context
     * @return the set of variables
     */
    public Set<String> getCalled() {
        return called;
    }
}
