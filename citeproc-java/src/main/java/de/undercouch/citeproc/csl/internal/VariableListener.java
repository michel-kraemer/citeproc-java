package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLName;

/**
 * A variable listener will be called when a variable is fetched from the
 * {@link RenderContext}
 * @author Michel Kraemer
 */
public interface VariableListener {
    /**
     * Will be called when a string variable is fetched from the context
     * @param name the variable's name
     * @param value the variable's value (may be {@code null}
     */
    void onFetchStringVariable(String name, String value);

    /**
     * Will be called when a name variable is fetched from the context
     * @param name the variable's name
     * @param value the variable's value (may be {@code null}
     */
    void onFetchNameVariable(String name, CSLName[] value);

    /**
     * Will be called when a date variable is fetched from the context
     * @param name the variable's name
     * @param value the variable's value (may be {@code null}
     */
    void onFetchDateVariable(String name, CSLDate value);
}
