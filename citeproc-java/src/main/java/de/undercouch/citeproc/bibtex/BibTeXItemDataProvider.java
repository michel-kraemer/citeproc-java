package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;
import org.jbibtex.BibTeXDatabase;

/**
 * Loads citation items from a BibTeX database
 * @author Michel Kraemer
 */
public class BibTeXItemDataProvider extends ListItemDataProvider {
    /**
     * Adds the given database
     * @param db the database to add
     */
    public void addDatabase(BibTeXDatabase db) {
        items.putAll(new BibTeXConverter().toItemData(db));
    }

    /**
     * Introduces all citation items from the BibTeX databases added
     * via {@link #addDatabase(BibTeXDatabase)} to the given CSL processor
     * @see CSL#registerCitationItems(String[])
     * @param citeproc the CSL processor
     */
    public void registerCitationItems(CSL citeproc) {
        citeproc.registerCitationItems(getIds());
    }
}
