package de.undercouch.citeproc.csl.internal;

/**
 * This enumeration is used to select a specified form of a variable
 * @author Michel Kraemer
 */
public enum VariableForm {
    /**
     * Select the long form of the variable
     */
    LONG,

    /**
     * Select the short form of the variable
     */
    SHORT;

    /**
     * Parse the variable form from a string
     * @param str the string to parse
     * @return the variable form
     */
    public static VariableForm fromString(String str) {
        if ("short".equals(str)) {
            return SHORT;
        }
        return LONG;
    }
}
