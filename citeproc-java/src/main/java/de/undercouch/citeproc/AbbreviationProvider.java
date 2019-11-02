package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLAbbreviationList;

/**
 * Retrieves abbreviations for titles, authorities, institution names, etc.
 * @author Michel Kraemer
 */
public interface AbbreviationProvider {
    /**
     * A list name that can be used for default abbreviations that do not
     * have to be enabled via {@link CSL#setAbbreviations(String)}
     */
    String DEFAULT_LIST_NAME = "default";

    /**
     * Retrieves an abbreviation list with a given name
     * @param name the list's name
     * @return the abbreviation list or null if there is no such list
     */
    CSLAbbreviationList getAbbreviations(String name);
}
