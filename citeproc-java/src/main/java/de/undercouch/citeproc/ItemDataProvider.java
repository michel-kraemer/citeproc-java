package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;

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
     * @return an array of all item IDs this provider can serve
     */
    String[] getIds();
}
