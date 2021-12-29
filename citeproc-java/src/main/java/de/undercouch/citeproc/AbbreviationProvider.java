package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * Retrieves abbreviations for titles, authorities, institution names, etc.
 * @author Michel Kraemer
 */
public interface AbbreviationProvider {
    /**
     * Retrieves an abbreviation for a given variable
     * @param variable the name of the variable
     * @param original the original (unabbreviated) string
     * @param item the current CSL item data
     * @return the abbreviated string or {@code null} if the original string
     * should not be abbreviated
     */
    String getAbbreviation(String variable, String original, CSLItemData item);
}
