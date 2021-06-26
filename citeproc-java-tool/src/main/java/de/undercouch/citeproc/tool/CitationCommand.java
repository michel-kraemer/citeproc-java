package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.output.Citation;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

/**
 * CLI command that generates citations which can be inserted into the text
 * @author Michel Kraemer
 */
public class CitationCommand extends BibliographyCommand {
    @Override
    public String getUsageName() {
        return "citation";
    }

    @Override
    public String getUsageDescription() {
        return "Generate citations from an input file";
    }

    @Override
    protected void doGenerateCSL(CSL citeproc, Collection<String> citationIds,
            PrintWriter out) {
        List<Citation> cits = citeproc.makeCitation(citationIds);
        for (Citation c : cits) {
            out.println(c.getText());
        }
    }
}
