package $pkg;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;

/**
 * Loads citation items from a $desc library
 * @author Michel Kraemer
 */
public class ${desc}ItemDataProvider extends ListItemDataProvider {
    /**
     * Adds the given library
     * @param lib the library to add
     */
    public void addLibrary(${desc}Library lib) {
        items.putAll(new ${desc}Converter().toItemData(lib));
    }
    
    /**
     * Introduces all citation items from the $desc libraries added
     * via {@link #addLibrary(${desc}Library)} to the given CSL processor
     * @see CSL#registerCitationItems(String[])
     * @param citeproc the CSL processor
     */
    public void registerCitationItems(CSL citeproc) {
        citeproc.registerCitationItems(getIds());
    }
}
