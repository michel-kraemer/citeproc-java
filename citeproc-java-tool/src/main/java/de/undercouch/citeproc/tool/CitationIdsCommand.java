package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.helper.Levenshtein;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.UnknownAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A base class for commands that accept citation IDs as arguments
 * @author Michel Kraemer
 */
public abstract class CitationIdsCommand extends AbstractCSLToolCommand implements ProviderCommand {
    /**
     * The item data provider
     */
    private ItemDataProvider provider;

    /**
     * The citation IDs
     */
    private List<String> citationIds = new ArrayList<>();

    /**
     * Sets the citation IDs
     * @param ids the IDs
     */
    @UnknownAttributes("CITATION ID")
    public void setCitationIds(List<String> ids) {
        citationIds = ids;
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws IOException {
        // check provided citation ids
        if (!checkCitationIds(citationIds, getProvider())) {
            return 1;
        }

        return 0;
    }

    /**
     * @return the citation IDs
     */
    protected List<String> getCitationIds() {
        return citationIds;
    }

    /**
     * Checks the citation IDs provided on the command line
     * @param citationIds the citation IDs
     * @param provider the item data provider
     * @return true if all citation IDs are OK, false if they're not
     */
    protected boolean checkCitationIds(List<String> citationIds, ItemDataProvider provider) {
        for (String id : citationIds) {
            if (provider.retrieveItem(id) == null) {
                StringBuilder message = new StringBuilder("unknown citation id: " + id);

                // find alternatives
                Collection<String> availableIds = provider.getIds();
                if (!availableIds.isEmpty()) {
                    Collection<String> mins = Levenshtein.findSimilar(availableIds, id);
                    if (mins.size() > 0) {
                        if (mins.size() == 1) {
                            message.append("\n\nDid you mean this?");
                        } else {
                            message.append("\n\nDid you mean one of these?");
                        }
                        for (String m : mins) {
                            message.append("\n\t").append(m);
                        }
                    }
                }

                error(message.toString());

                return false;
            }
        }
        return true;
    }

    @Override
    public ItemDataProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(ItemDataProvider provider) {
        this.provider = provider;
    }
}
