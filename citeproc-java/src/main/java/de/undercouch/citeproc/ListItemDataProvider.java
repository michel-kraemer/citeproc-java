package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.Arrays;
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
    protected Map<String, CSLItemData> items = new LinkedHashMap<>();

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
        for (CSLItemData i : items) {
            this.items.put(i.getId(), i);
        }
    }

    @Override
    public CSLItemData retrieveItem(String id) {
        return items.get(id);
    }

    @Override
    public String[] getIds() {
        String[] ids = new String[items.size()];
        int i = 0;
        for (Map.Entry<String, CSLItemData> e : items.entrySet()) {
            ids[i++] = e.getKey();
        }
        return ids;
    }
}
