package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides item data from a given list
 * @author Michel Kraemer
 */
public class ListItemDataProvider implements ItemDataProvider {
    /**
     * The items that this provider holds
     */
    protected final Map<String, CSLItemData> items;

    /**
     * Creates a data provider that serves items from the given array
     * @param items the items to serve
     */
    public ListItemDataProvider(CSLItemData... items) {
        this(Arrays.asList(items));
    }

    /**
     * Creates a data provider that serves items from the given list
     * @param items the items to serve
     */
    public ListItemDataProvider(List<CSLItemData> items) {
        Map<String, CSLItemData> is = new LinkedHashMap<>();
        for (CSLItemData i : items) {
            is.put(i.getId(), i);
        }
        this.items = is;
    }

    @Override
    public CSLItemData retrieveItem(String id) {
        return items.get(id);
    }

    @Override
    public Collection<String> getIds() {
        return Collections.unmodifiableSet(items.keySet());
    }
}
