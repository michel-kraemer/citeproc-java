package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * A command that uses an {@link ItemDataProvider} to get citation data
 * @author Michel Kraemer
 */
public interface ProviderCommand extends CSLToolCommand {
    /**
     * @return the item data provider holding input citation data
     */
    ItemDataProvider getProvider();

    /**
     * Sets the item data provider holding input citation data
     * @param provider the provider
     */
    void setProvider(ItemDataProvider provider);
}
