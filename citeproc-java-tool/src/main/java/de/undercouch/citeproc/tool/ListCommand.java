package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.underline.InputReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CLI command that lists items from an input bibliography
 * @author Michel Kraemer
 */
public class ListCommand extends AbstractCSLToolCommand implements ProviderCommand {
    /**
     * The item data provider
     */
    private ItemDataProvider provider;

    @Override
    public String getUsageName() {
        return "list";
    }

    @Override
    public String getUsageDescription() {
        return "Display sorted list of available citation IDs";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws IOException {
        // list available citation ids and exit
        List<String> ids = new ArrayList<>(getProvider().getIds());
        Collections.sort(ids);
        for (String id : ids) {
            out.println(id);
        }

        return 0;
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
