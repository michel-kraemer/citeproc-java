package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;
import de.undercouch.underline.InputReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

/**
 * CLI command that converts input bibliographies to JSON files
 * @author Michel Kraemer
 */
public class JsonCommand extends CitationIdsCommand {
    @Override
    public String getUsageName() {
        return "json";
    }

    @Override
    public String getUsageDescription() {
        return "Convert input bibliography to JSON";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws IOException {
        int ret = super.doRun(remainingArgs, in, out);
        if (ret != 0) {
            return ret;
        }

        // run conversion
        generateJSON(getCitationIds(), getProvider(), out);

        return 0;
    }

    /**
     * Generates JSON
     * @param citationIds the citation ids given on the command line
     * @param provider a provider containing all citation item data
     * @param out the print stream to write the output to
     */
    private void generateJSON(List<String> citationIds, ItemDataProvider provider,
            PrintWriter out) {
        StringJsonBuilderFactory factory = new StringJsonBuilderFactory();
        // create an array of citation item data objects (either for
        // the whole bibliography or for the given citation ids only)
        out.print("[");
        Collection<String> ids = citationIds;
        if (ids.isEmpty()) {
            ids = provider.getIds();
        }

        int i = 0;
        for (String id : ids) {
            if (i > 0) {
                out.print(",");
            }
            CSLItemData item = provider.retrieveItem(id);
            JsonBuilder b = factory.createJsonBuilder();
            out.print(item.toJson(b));
            ++i;
        }

        out.println("]");
    }
}
