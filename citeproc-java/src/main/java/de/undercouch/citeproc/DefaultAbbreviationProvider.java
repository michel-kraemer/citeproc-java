package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link AbbreviationProvider}
 * @author Michel Kraemer
 */
public class DefaultAbbreviationProvider implements AbbreviationProvider {
    private final Map<String, Map<String, String>> abbrevs = new HashMap<>();

    public void addAbbreviation(String variable, String original, String abbreviation) {
        Map<String, String> vm = abbrevs.computeIfAbsent(variable, v -> new HashMap<>());
        vm.put(original, abbreviation);
    }

    @Override
    public String getAbbreviation(String variable, String original, CSLItemData item) {
        Map<String, String> vm = abbrevs.get(variable);
        if (vm != null) {
            return vm.get(original);
        }
        return null;
    }
}
