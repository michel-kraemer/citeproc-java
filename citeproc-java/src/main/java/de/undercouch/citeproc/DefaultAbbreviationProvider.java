package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLAbbreviationList;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link AbbreviationProvider}
 * @author Michel Kraemer
 */
public class DefaultAbbreviationProvider implements AbbreviationProvider {
    private final Map<String, CSLAbbreviationList> lists = new HashMap<>();

    /**
     * Adds an abbreviation list to this provider
     * @param name the list's name
     * @param list the list
     */
    public void add(String name, CSLAbbreviationList list) {
        lists.put(name, list);
    }

    @Override
    public CSLAbbreviationList getAbbreviations(String name) {
        return lists.get(name);
    }
}
