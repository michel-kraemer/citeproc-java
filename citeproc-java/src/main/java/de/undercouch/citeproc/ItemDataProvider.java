package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.Collection;

/**
 * Retrieves citation items
 * @author Michel Kraemer
 */
public interface ItemDataProvider {
    /**
     * Retrieve a citation item with a given ID
     * @param id the item's unique ID
     * @return the item
     */
    CSLItemData retrieveItem(String id);

    /**
     * @return all item IDs this provider can serve
     */
    Collection<String> getIds();
}
